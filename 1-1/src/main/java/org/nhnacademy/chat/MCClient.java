package org.nhnacademy.chat;


import java.io.IOException;
import java.net.Socket;
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
 * 터미널에서 동작하는 채팅 클라이언트 입니다.
 * UserID의 기본값은 user 입니다.
 */
@SuppressWarnings({"checkstyle:AbbreviationAsWordInName"}) // 클래스 네이밍 검사 억제,
public class MCClient {

    private static final Logger logger = LogManager.getLogger(MCClient.class);

    /**
     * The entry point of application.
     *
     * @param args the input arguments, -h 으로 도움말을 표시 할 수 있습니다.
     */
    public static void main(String[] args) {
        // log4j 기본 설정으로 초기화.
        Configurator.initialize(new DefaultConfiguration());
        Configurator.setRootLevel(Level.DEBUG); // DEBUG 레벨 보이도록 설정.

        Options options = new Options();
        options.addOption(Option.builder()
            .option("H")
            .hasArg()
            .argName("host")
            .desc("접속할 서버의 호스트 네임 또는 IP를 지정합니다.")
            .build());
        options.addOption("h", false, "도움말");
        options.addOption(Option.builder()
            .option("I")
            .hasArg()
            .argName("user id")
            .desc("채팅에서 사용할 사용자 아이디를 지정합니다.")
            .build());
        options.addOption(Option.builder()
            .option("p")
            .hasArg()
            .argName("port")
            .desc("접속할 서버의 서비스 포트를 지정합니다.")
            .build());

        HelpFormatter helpFormatter = new HelpFormatter();
        try {
            CommandLine cmd = new DefaultParser().parse(options, args);

            if (cmd.hasOption("h")) {
                helpFormatter.printHelp(MCClient.class.getSimpleName(), options);
                return;
            }

            // 사전 오류 검사 시작
            StringBuilder errMsg = new StringBuilder();
            if (!cmd.hasOption('H')) {
                errMsg.append("-H 옵션은 필수입니다.")
                    .append("\n");
            }

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

            String name = "user";

            if (cmd.hasOption('I')) {
                if (cmd.getOptionValue('I').equalsIgnoreCase("all")) {
                    errMsg.append("all 이름은 사용 할 수 없습니다.")
                        .append("\n");
                } else {
                    name = cmd.getOptionValue('I');
                }
            }

            if (errMsg.length() > 0) {
                throw new ParseException(errMsg.toString());
            }
            // 사전 오류 검사 끝

            // 서버와 연결
            String host = cmd.getOptionValue('H');
            int port = Integer.parseInt(cmd.getOptionValue('p'));

            Socket socket = new Socket(host, port);
            User user = new User(name, socket);
            user.run();
            // 서버와 연결 끝

        } catch (ParseException e) {
            logger.error("명령어 인수가 잘못되었습니다.");
            logger.error(e.getMessage());
            helpFormatter.printHelp(MCClient.class.getSimpleName(), options);
        } catch (NumberFormatException e) {
            logger.error("포트번호는 숫자만 입력해주세요.");
            logger.error(e.getMessage());
            helpFormatter.printHelp(MCClient.class.getSimpleName(), options);
        } catch (IOException e) {
            logger.error("서버와의 연결이 끊어졌습니다.");
        }
    }
}
