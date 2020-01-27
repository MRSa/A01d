package net.osdn.gokigen.a01d.camera.ptpip.wrapper.status;

import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommand;
import net.osdn.gokigen.a01d.camera.ptpip.wrapper.command.IPtpIpCommandCallback;

public interface IPtpIpInitEventRequest
{
    IPtpIpCommand getInitEventRequest(IPtpIpCommandCallback callback,  int connectionNumber);
}
