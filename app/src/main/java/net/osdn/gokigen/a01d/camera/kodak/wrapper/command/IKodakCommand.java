package net.osdn.gokigen.a01d.camera.kodak.wrapper.command;


public interface IKodakCommand
{
    // メッセージの識別子
    int getId();

    // コマンドの受信待ち時間(単位:ms)
    int receiveDelayMs();

    // 送信するメッセージボディ
    byte[] commandBody();

    // 送信するメッセージボディ(連続送信する場合)
    byte[] commandBody2();

    // 受信待ち再試行回数
    int maxRetryCount();

    // コマンドの受信が失敗した場合、再送する（再送する場合は true）
    boolean sendRetry();

    // コマンド送信結果（応答）の通知先
    IKodakCommandCallback responseCallback();

    // デバッグ用： ログ(logcat)に通信結果を残すかどうか
    boolean dumpLog();

}
