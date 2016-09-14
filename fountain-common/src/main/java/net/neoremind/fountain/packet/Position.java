package net.neoremind.fountain.packet;

/**
 * 字节数组位置
 *
 * @author zhangxu
 */
public class Position {

    private int currPos;

    public Position() {

    }

    public Position(int currPos) {
        this.currPos = currPos;
    }

    public synchronized void increase() {
        currPos++;
    }

    public synchronized void increase(int offset) {
        currPos += offset;
    }

    public int getPosition() {
        return currPos;
    }
}
