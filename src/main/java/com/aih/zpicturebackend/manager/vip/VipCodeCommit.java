package com.aih.zpicturebackend.manager.vip;

@FunctionalInterface
public interface VipCodeCommit {

    boolean commit(VipCodeRedeemResult redeemResult);
}
