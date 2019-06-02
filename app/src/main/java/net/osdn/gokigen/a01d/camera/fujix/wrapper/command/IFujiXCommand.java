package net.osdn.gokigen.a01d.camera.fujix.wrapper.command;

public interface IFujiXCommand
{
    // メッセージの識別子
    int getId();

    // シーケンス番号を埋め込むかどうか
    boolean useSequenceNumber();

    // シーケンス番号を更新（＋１）するかどうか
    boolean isIncrementSeqNumber();

    // コマンドの受信待ち時間(単位:ms)
    int receiveDelayMs();

    // 送信するメッセージボディ
    byte[] commandBody();

    // 送信するメッセージボディ(連続送信する場合)
    byte[] commandBody2();

    // コマンド送信結果（応答）の通知先
    IFujiXCommandCallback responseCallback();

    // デバッグ用： ログ(logcat)に通信結果を残すかどうか
    boolean dumpLog();
}
