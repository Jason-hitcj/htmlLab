import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class MainTest {
    private ByteArrayOutputStream outContent;
    private ByteArrayInputStream inContent;

    @Before
    public void setUp() {
        // 重置输出流
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @Test
    public void testInitCommand() {
        // 模拟用户输入 init 和 exit 命令
        String input = "init\nexit\n";
        inContent = new ByteArrayInputStream(input.getBytes());
        System.setIn(inContent);

        Main.main(new String[]{});

        // 验证初始化成功的输出
        assertTrue(outContent.toString().contains("Welcome to the HTML Editor!"));
    }

    @Test
    public void testInsertAndAppendCommands() {
        String input = "init\n" +
                "insert div testDiv body \"Test Div Content\"\n" +
                "append p testPara body \"Paragraph Content\"\n" +
                "print-tree\n" +
                "exit\n";
        inContent = new ByteArrayInputStream(input.getBytes());
        System.setIn(inContent);

        Main.main(new String[]{});

        // 验证命令执行结果
        String output = outContent.toString();
        assertTrue(output.contains("div#testDiv"));
        assertTrue(output.contains("p#testPara"));
    }

    @Test
    public void testEditAndDeleteCommands() {
        String input = "init\n" +
                "insert div editDiv body \"Original Content\"\n" +
                "edit-text editDiv \"Updated Content\"\n" +
                "edit-id editDiv newEditDiv\n" +
                "delete newEditDiv\n" +
                "print-tree\n" +
                "exit\n";
        inContent = new ByteArrayInputStream(input.getBytes());
        System.setIn(inContent);

        Main.main(new String[]{});

        // 验证命令执行的正确性
        String output = outContent.toString();
        assertTrue(output.contains("Updated Content"));
    }

    @Test
    public void testUndoRedoCommands() {
        String input = "init\n" +
                "insert div undoDiv body \"Test Div\"\n" +
                "undo\n" +
                "print-tree\n" +
                "redo\n" +
                "print-tree\n" +
                "exit\n";
        inContent = new ByteArrayInputStream(input.getBytes());
        System.setIn(inContent);

        Main.main(new String[]{});

        // 验证撤销和重做功能
        String output = outContent.toString();
        // 具体的验证逻辑取决于您的实现
    }

    @Test
    public void testFileOperations() {
        String input = "init\n" +
                "insert div fileDiv body \"File Test\"\n" +
                "save test_output1.html\n" +
                "read test_output1.html\n" +
                "print-tree\n" +
                "exit\n";
        inContent = new ByteArrayInputStream(input.getBytes());
        System.setIn(inContent);

        Main.main(new String[]{});

        // 验证文件读写操作
        String output = outContent.toString();
        assertTrue(output.contains("div#fileDiv"));
    }

}