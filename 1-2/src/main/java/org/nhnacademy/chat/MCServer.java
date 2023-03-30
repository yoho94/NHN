package org.nhnacademy.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;

/**
 * 터미널에서 동작하는 채팅 서버.
 */
@SuppressWarnings({"checkstyle:AbbreviationAsWordInName"}) // 클래스 네이밍 검사 억제,
public class MCServer {

    private static final Logger logger = LogManager.getLogger(MCServer.class);
    private static final int MAX_CONNECT_COUNT = 10;

    /**
     * The entry point of application.
     *
     * @param args the input arguments, -h 으로 도움말을 표시 할 수 있습니다.
     */
    public static void main(String[] args) {
        // log4j 기본 설정으로 초기화.
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG); // DEBUG 출력.

        Options options = new Options();
        options.addOption(Option.builder()
            .option("b")
            .hasArg()
            .argName("arg")
            .desc("블랙리스트")
            .build());
        options.addOption("h", false, "도움말");
        options.addOption(Option.builder()
            .option("p")
            .hasArg()
            .argName("arg")
            .desc("서비스 포트")
            .build());

        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            CommandLine cmd = new DefaultParser().parse(options, args);

            if (cmd.hasOption("h")) {
                helpFormatter.printHelp("MCServer", options);
                return;
            }

            // 사전 오류 검사 시작
            StringBuilder errMsg = new StringBuilder();

            if (!cmd.hasOption('p')) {
                errMsg.append("-p 옵션은 필수입니다.")
                    .append("\n");
            } else {
                int port = Integer.parseInt(cmd.getOptionValue('p'));

                if (port < 0 || port > 65535) {
                    errMsg.append("사용 가능한 포트번호의 범위는 0 ~ 65535 입니다.")
                        .append("\n");
                }
            }

            if (errMsg.length() > 0) {
                throw new ParseException(errMsg.toString());
            }
            // 사전 오류 검사 끝
            // 블랙 리스트 추가
            if (cmd.hasOption('b')) {
                String[] blackListArray = cmd.getOptionValue('b').split(",");

                Handler.blackList.addAll(Arrays.asList(blackListArray));
            }
            // 블랙 리스트 추가 끝

            int port = Integer.parseInt(cmd.getOptionValue('p'));
            ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CONNECT_COUNT);

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                // 서버 시작
                while (!Thread.currentThread().isInterrupted()) {
                    logger.debug("접속 대기중 ...");
                    threadPool.execute(new Handler(serverSocket.accept()));
                    logger.debug("접속 완료");
                }
            }


        } catch (ParseException e) {
            logger.error("명령어 인수가 잘못되었습니다.");
            logger.error(e.getMessage());
            helpFormatter.printHelp("MCClient", options);
        } catch (NumberFormatException e) {
            logger.error("포트번호는 숫자만 입력해주세요.");
            logger.error(e.getMessage());
            helpFormatter.printHelp("MCClient", options);
        } catch (IOException e) {
            logger.error("서버와의 연결이 끊어졌습니다.");
        }
    }
}
