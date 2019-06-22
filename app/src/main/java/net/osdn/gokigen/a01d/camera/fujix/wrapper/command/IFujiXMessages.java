package net.osdn.gokigen.a01d.camera.fujix.wrapper.command;

public interface IFujiXMessages
{
    int SEQ_DUMMY = 0;
    int SEQ_REGISTRATION = 1;
    int SEQ_START = 2;
    int SEQ_START_2ND = 3;
    int SEQ_START_2ND_READ = 10;
    int SEQ_START_2ND_RECEIVE = 4;
    int SEQ_START_3RD = 5;
    int SEQ_START_4TH = 6;
    int SEQ_CAMERA_REMOTE = 7;
    int SEQ_START_5TH = 8;
    int SEQ_STATUS_REQUEST = 9;
    int SEQ_QUERY_CAMERA_CAPABILITIES = 11;

    int SEQ_CHANGE_TO_PLAYBACK_1ST = 12;
    int SEQ_CHANGE_TO_PLAYBACK_2ND = 13;
    int SEQ_CHANGE_TO_PLAYBACK_3RD = 14;
    int SEQ_CHANGE_TO_PLAYBACK_4TH = 15;

    int SEQ_CHANGE_TO_LIVEVIEW_1ST = 16;
    int SEQ_CHANGE_TO_LIVEVIEW_2ND = 17;
    int SEQ_CHANGE_TO_LIVEVIEW_3RD = 18;
    int SEQ_CHANGE_TO_LIVEVIEW_4TH = 19;
    int SEQ_CHANGE_TO_LIVEVIEW_5TH = 20;


}
