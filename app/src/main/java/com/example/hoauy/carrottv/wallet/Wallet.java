package com.example.hoauy.carrottv.wallet;

import android.os.Environment;
import android.util.Log;

import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

/**
 *
 *  1. 정의
 *
 *  지갑에 관한 메소드들
 *
 *
 *
 *  2. 주의점
 *
 *  1) 그대로 가져온 것이기 때문에 수정할 필요는 없음.
 *
 *  2) createWallet() : 지갑 생성
 *     loadCredentials() : password 입력하면 기기에 있는 지갑의 주소를 return
 *     constructWeb3() : 테스트넷에 올라와있는 내 토큰의 계약서 주소와 연결해줌
 *
 *  3) 2)의 3개 메소드들만 현재 사용 중
 *
 *
 */


public class Wallet {


    public String createWallet() throws Exception {
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
        String fileName = WalletUtils.generateLightNewWalletFile("password", new File(path));
        return path +"/"+fileName;
    }

    public Credentials loadCredentials(String password, String path) throws Exception {
        Credentials credentials = WalletUtils.loadCredentials(
                password,
                path);
        Log.i("Loading credentials", "Credentials loaded");
        return credentials;
    }

    public Web3j constructWeb3(String URL) throws IOException {
        Web3j web3 = Web3jFactory.build(new HttpService(URL));  // defaults to http://localhost:8545/
        Web3ClientVersion web3ClientVersion;
        web3ClientVersion = web3.web3ClientVersion().send();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        Log.e("Web3 verison", clientVersion);
        return web3;
    }

    public String sendTransaction(Web3j web3, Credentials credentials) throws Exception {
        TransactionReceipt transferReceipt = Transfer.sendFunds(web3, credentials,
                "0x19e03255f667bdfd50a32722df860b1eeaf4d635",  // you can put any address here
                BigDecimal.ONE, Convert.Unit.WEI)  // 1 wei = 10^-18 Ether
                .send();
        return transferReceipt.getTransactionHash();
    }

    public String createBipWallet() throws Exception {
        String path = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
        Bip39Wallet bip39Wallet = WalletUtils.generateBip39Wallet("password", new File(path));
        String filename = bip39Wallet.getFilename();
        String mnemonic = bip39Wallet.getMnemonic();
        return "Success";
    }

    public void checkWalletExist() {

    }

}
