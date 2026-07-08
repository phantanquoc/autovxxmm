/*
 * Decompiled with CFR 0.152.
 */
package lib;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class mSocket {
    private Socket socket;

    public mSocket(String ip, int port) {
        try {
            this.socket = new Socket(ip, port);
            // SO_TIMEOUT: qua đêm server có thể rớt TCP half-open (không FIN/RST, NAT idle
            // timeout) -> readByte() block vô hạn -> bot "online giả" treo mãi. Đặt timeout
            // 90s để readMessage() ném SocketTimeoutException -> DataReceiver thoát ->
            // closeConnectionAsynchronous() -> reconnect. 90s > nhịp gói server bình thường
            // nên không cắt nhầm kết nối đang khoẻ.
            this.socket.setSoTimeout(90000);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void close() {
        try {
            this.socket.close();
            this.socket = null;
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public DataOutputStream getOutputStream() {
        try {
            OutputStream os = this.socket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            return dos;
        }
        catch (Exception e) {
            return null;
        }
    }

    public DataInputStream getInputStream() {
        try {
            InputStream is = this.socket.getInputStream();
            DataInputStream dis = new DataInputStream(is);
            return dis;
        }
        catch (Exception e) {
            return null;
        }
    }
}

