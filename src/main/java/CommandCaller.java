import Commands.Command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandCaller {

    private static String[] splitInput(String input) {
        // 使用正则表达式来匹配命令参数，包括可能被引号包围的文本内容
        Pattern pattern = Pattern.compile(
                "(\"[^\"]*\"|'[^']*'|\\S+)"  // 匹配被双引号或单引号包围的内容，或者非空白字符序列
        );
        Matcher matcher = pattern.matcher(input);

        // 收集所有匹配项
        int count = 0;
        while (matcher.find()) {
            count++;
        }

        // 创建结果数组
        String[] result = new String[count];
        matcher.reset();
        for (int i = 0; i < count && matcher.find(); i++) {
            // 去掉引号
            result[i] = matcher.group().replaceAll("^\"|\"$|^'|'$", "");
        }

        return result;
    }

    public static Command createCommand(String input) {
        String[] splitInput = splitInput(input);
        String commandType = splitInput[0];
        int newArrayLength = splitInput.length - 1;
        String[] commandArgs = new String[newArrayLength];
        if(splitInput.length > 1){
            System.arraycopy(splitInput, 1, commandArgs, 0, newArrayLength);
        }
        Command command = CommandFactory.createCommand(commandType, commandArgs);
        return command;
    }

    public void executeCommand(String input) {
        switch (input) {
            case "uninitialized": {
                System.out.println("Uninitialized! Use init or read first");
                break;
            }
            case "unknown": {
                System.out.println("Unknown command!");
                break;
            }
            default: {
                try{
                    Command command = createCommand(input);
                    command.execute();
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }
        };
    }
}
