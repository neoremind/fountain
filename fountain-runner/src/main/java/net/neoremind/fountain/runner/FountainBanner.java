package net.neoremind.fountain.runner;

import static org.springframework.boot.ansi.AnsiElement.DEFAULT;
import static org.springframework.boot.ansi.AnsiElement.FAINT;
import static org.springframework.boot.ansi.AnsiElement.GREEN;

import java.io.PrintStream;

import org.springframework.boot.Banner;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.env.Environment;

/**
 * 通过入口{@link FountainMain}启动fountain时候，打印在控制台上的logo，通过覆盖Spring boot的默认logo实现
 * <p/>
 * 参考以下链接生成：<br/>
 * <a href="http://osxdaily.com/2014/05/23/create-ascii-art-text-banner/">create-ascii-art-text-banner</a><br/>
 * <a href="http://www.network-science.de/ascii/">ascii</a>
 *
 * @author zhangxu
 */
public class FountainBanner implements Banner {

    private static final String[] BANNER = {""
            + " / _|                | |      (_)      \n"
            + "| |_ ___  _   _ _ __ | |_ __ _ _ _ __  \n"
            + "|  _/ _ \\| | | | '_ \\| __/ _` | | '_ \\ \n"
            + "| || (_) | |_| | | | | || (_| | | | | |\n"
            + "|_| \\___/ \\__,_|_| |_|\\__\\__,_|_|_| |_|"};

    private static final String FOUNTAIN = " :: Fountain :: ";

    private static final int STRAP_LINE_SIZE = 42;

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass,
                            PrintStream printStream) {
        for (String line : BANNER) {
            printStream.println(line);
        }
        String version = FountainBanner.class.getPackage().getImplementationVersion();
        version = (version == null ? "" : " (v" + version + ")");
        String padding = "";
        while (padding.length() < STRAP_LINE_SIZE
                - (version.length() + FOUNTAIN.length())) {
            padding += " ";
        }

        printStream.println(AnsiOutput.toString(GREEN, FOUNTAIN, DEFAULT, padding,
                FAINT, version));
        printStream.println();
    }

}
