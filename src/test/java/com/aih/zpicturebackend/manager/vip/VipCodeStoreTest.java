package com.aih.zpicturebackend.manager.vip;

import com.aih.zpicturebackend.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VipCodeStoreTest {

    @TempDir
    Path tempDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void redeemMarksCodeUsedAndRejectsReuse() throws Exception {
        Path codeFile = tempDir.resolve("vip-codes.json");
        Files.write(codeFile, ("[" +
                "{\"code\":\"VIP-TEST-0001\",\"used\":false,\"expireTime\":\"2099-12-31 23:59:59\",\"durationDays\":30,\"vipLevel\":2}" +
                "]").getBytes(StandardCharsets.UTF_8));
        VipCodeStore vipCodeStore = new VipCodeStore(codeFile, objectMapper);

        VipCodeRedeemResult result = vipCodeStore.redeem(" VIP-TEST-0001 ", 1001L, now(), redeemResult -> true);

        assertEquals("VIP-TEST-0001", result.getCode());
        assertEquals(30, result.getDurationDays());
        assertEquals(2, result.getVipLevel());
        List<VipCodeRecord> records = readRecords(codeFile);
        assertTrue(records.get(0).isUsed());
        assertEquals(Long.valueOf(1001L), records.get(0).getUsedUserId());
        assertThrows(BusinessException.class,
                () -> vipCodeStore.redeem("VIP-TEST-0001", 1002L, now(), redeemResult -> true));
    }

    @Test
    void redeemUsesProfessionalVipLevelWhenCodeOmitsLevel() throws Exception {
        Path codeFile = tempDir.resolve("vip-codes.json");
        Files.write(codeFile, ("[" +
                "{\"code\":\"VIP-TEST-DEFAULT\",\"used\":false,\"expireTime\":\"2099-12-31 23:59:59\",\"durationDays\":30}" +
                "]").getBytes(StandardCharsets.UTF_8));
        VipCodeStore vipCodeStore = new VipCodeStore(codeFile, objectMapper);

        VipCodeRedeemResult result = vipCodeStore.redeem("VIP-TEST-DEFAULT", 1001L, now(), redeemResult -> true);

        assertEquals(1, result.getVipLevel());
    }

    @Test
    void redeemRejectsExpiredCodeWithoutMarkingItUsed() throws Exception {
        Path codeFile = tempDir.resolve("vip-codes.json");
        Files.write(codeFile, ("[" +
                "{\"code\":\"VIP-TEST-EXPIRED\",\"used\":false,\"expireTime\":\"2000-01-01 00:00:00\",\"durationDays\":30}" +
                "]").getBytes(StandardCharsets.UTF_8));
        VipCodeStore vipCodeStore = new VipCodeStore(codeFile, objectMapper);

        assertThrows(BusinessException.class,
                () -> vipCodeStore.redeem("VIP-TEST-EXPIRED", 1001L, now(), redeemResult -> true));

        List<VipCodeRecord> records = readRecords(codeFile);
        assertEquals(false, records.get(0).isUsed());
    }

    @Test
    void redeemRollsBackCodeWhenCommitFails() throws Exception {
        Path codeFile = tempDir.resolve("vip-codes.json");
        Files.write(codeFile, ("[" +
                "{\"code\":\"VIP-TEST-ROLLBACK\",\"used\":false,\"expireTime\":\"2099-12-31 23:59:59\",\"durationDays\":30}" +
                "]").getBytes(StandardCharsets.UTF_8));
        VipCodeStore vipCodeStore = new VipCodeStore(codeFile, objectMapper);

        assertThrows(BusinessException.class,
                () -> vipCodeStore.redeem("VIP-TEST-ROLLBACK", 1001L, now(), redeemResult -> false));

        List<VipCodeRecord> records = readRecords(codeFile);
        assertEquals(false, records.get(0).isUsed());
    }

    private Date now() throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2026-05-19 12:00:00");
    }

    private List<VipCodeRecord> readRecords(Path codeFile) throws Exception {
        return objectMapper.readValue(codeFile.toFile(), new TypeReference<List<VipCodeRecord>>() {
        });
    }
}
