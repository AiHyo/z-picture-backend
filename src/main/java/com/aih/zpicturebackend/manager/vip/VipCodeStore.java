package com.aih.zpicturebackend.manager.vip;

import cn.hutool.core.util.StrUtil;
import com.aih.zpicturebackend.exception.BusinessException;
import com.aih.zpicturebackend.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VipCodeStore {

    private static final TypeReference<List<VipCodeRecord>> VIP_CODE_LIST_TYPE = new TypeReference<List<VipCodeRecord>>() {
    };

    private static final int DEFAULT_DURATION_DAYS = 365;

    private static final int DEFAULT_VIP_LEVEL = 1;

    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final Path codeFilePath;

    private final Path lockFilePath;

    private final ObjectMapper objectMapper;

    public VipCodeStore(Path codeFilePath, ObjectMapper objectMapper) {
        this.codeFilePath = codeFilePath;
        this.lockFilePath = codeFilePath.resolveSibling(codeFilePath.getFileName() + ".lock");
        this.objectMapper = objectMapper;
    }

    public VipCodeRedeemResult redeem(String vipCode, Long userId, Date now, VipCodeCommit commit) {
        if (StrUtil.isBlank(vipCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "兑换码为空");
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        ensureCodeFile();
        try (FileChannel channel = FileChannel.open(lockFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock ignored = channel.lock()) {
            List<VipCodeRecord> records = readRecords();
            VipCodeRecord matchedRecord = findRecord(records, vipCode.trim());
            validateRecord(matchedRecord, now);
            int durationDays = matchedRecord.getDurationDays() == null ? DEFAULT_DURATION_DAYS : matchedRecord.getDurationDays();
            int vipLevel = matchedRecord.getVipLevel() == null ? DEFAULT_VIP_LEVEL : matchedRecord.getVipLevel();
            VipCodeRedeemResult redeemResult = new VipCodeRedeemResult(matchedRecord.getCode(), durationDays, vipLevel);
            matchedRecord.setUsed(true);
            matchedRecord.setUsedUserId(userId);
            matchedRecord.setUsedTime(formatDate(now));
            writeRecords(records);
            commitRedeem(records, matchedRecord, redeemResult, commit);
            return redeemResult;
        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取兑换码失败");
        }
    }

    public int getVipLevel(String vipCode, Long userId) {
        if (StrUtil.isBlank(vipCode)) {
            return 0;
        }
        ensureCodeFile();
        try (FileChannel channel = FileChannel.open(lockFilePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock ignored = channel.lock()) {
            VipCodeRecord record = readRecords().stream()
                    .filter(item -> vipCode.trim().equals(item.getCode()))
                    .findFirst()
                    .orElse(null);
            if (record == null) {
                return 0;
            }
            if (!record.isUsed() || userId == null || !userId.equals(record.getUsedUserId())) {
                return 0;
            }
            Integer vipLevel = record.getVipLevel();
            if (vipLevel == null) {
                return DEFAULT_VIP_LEVEL;
            }
            return vipLevel >= 1 && vipLevel <= 2 ? vipLevel : 0;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "读取兑换码失败");
        }
    }

    private void commitRedeem(List<VipCodeRecord> records, VipCodeRecord matchedRecord,
                              VipCodeRedeemResult redeemResult, VipCodeCommit commit) throws IOException {
        try {
            boolean committed = commit.commit(redeemResult);
            if (!committed) {
                rollbackRedeem(records, matchedRecord);
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "会员兑换失败");
            }
        } catch (BusinessException e) {
            rollbackRedeem(records, matchedRecord);
            throw e;
        } catch (RuntimeException e) {
            rollbackRedeem(records, matchedRecord);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "会员兑换失败");
        }
    }

    private void rollbackRedeem(List<VipCodeRecord> records, VipCodeRecord matchedRecord) throws IOException {
        matchedRecord.setUsed(false);
        matchedRecord.setUsedUserId(null);
        matchedRecord.setUsedTime(null);
        writeRecords(records);
    }

    private void ensureCodeFile() {
        try {
            Path parent = codeFilePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (!Files.exists(codeFilePath)) {
                Files.write(codeFilePath, "[]".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
            }
            if (!Files.exists(lockFilePath)) {
                Files.write(lockFilePath, new byte[0], StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化兑换码文件失败");
        }
    }

    private List<VipCodeRecord> readRecords() throws IOException {
        if (Files.size(codeFilePath) == 0) {
            return new ArrayList<>();
        }
        return objectMapper.readValue(codeFilePath.toFile(), VIP_CODE_LIST_TYPE);
    }

    private void writeRecords(List<VipCodeRecord> records) throws IOException {
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(records);
        Files.write(codeFilePath, bytes, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    private VipCodeRecord findRecord(List<VipCodeRecord> records, String vipCode) {
        return records.stream()
                .filter(record -> vipCode.equals(record.getCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "兑换码无效"));
    }

    private void validateRecord(VipCodeRecord record, Date now) {
        if (record.isUsed()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "兑换码已使用");
        }
        if (record.getDurationDays() != null && record.getDurationDays() <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "兑换码文件格式错误");
        }
        if (record.getVipLevel() != null && (record.getVipLevel() < 1 || record.getVipLevel() > 2)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "兑换码文件格式错误");
        }
        if (StrUtil.isNotBlank(record.getExpireTime()) && parseDate(record.getExpireTime()).before(now)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "兑换码已过期");
        }
    }

    private Date parseDate(String dateText) {
        try {
            return new SimpleDateFormat(DATE_PATTERN).parse(dateText);
        } catch (ParseException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "兑换码文件格式错误");
        }
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }
}
