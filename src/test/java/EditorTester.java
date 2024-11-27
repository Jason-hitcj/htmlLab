import Utils.HtmlContext;
import Utils.HtmlElement;
import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class EditorTester {

    private Process editorProcess;

    @BeforeEach
    public void setUp() throws IOException {
        // 启动编辑器进程
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", "out/production/htmlLab", "Main");
        processBuilder.redirectErrorStream(true); // 将标准错误合并到标准输出
        editorProcess = processBuilder.start();
    }

    @Test
    public void testInitCommand() throws IOException, InterruptedException {
        // 向编辑器发送 'init' 命令
        sendCommand("init");

        // 获取编辑器返回的输出
//        String output = getOutput();
//        System.out.println("output for init is :\n" + output);
        sendCommand("get-context");
        Thread.sleep(500); // 确保子进程有时间处理命令
        String output = getOutput();
        System.out.println("编辑器输出内容为: " + output);

        // 断言 HTML 内容的 ID 不为 "empty"
        assertTrue(output.contains("initialized html root"));
    }

    @Test
    public void testInsertCommand() throws IOException, InterruptedException {
        // 向编辑器发送插入命令
        sendCommand("insert <div> 1 0 'Hello World'");

        // 获取打印树输出
        sendCommand("print-tree");
        String output = getOutput();
        assertTrue(output.contains("<div id=1>Hello World</div>"), "插入失败");
    }

    @Test
    public void testUndoWithoutInit() throws IOException, InterruptedException {
        sendCommand("undo");
        Thread.sleep(100);
        String output = getOutput();
        assertTrue(output.contains("Uninitialized! Use init or read first"),
                "Should prompt for initialization");
    }

    @Test
    public void testRedoClearAfterNewCommand() throws IOException, InterruptedException {
        // 初始化
        sendCommand("init");
        Thread.sleep(100);

        // 执行两个测试命令
        sendCommand("test 1");
        Thread.sleep(100);
        sendCommand("test 2");
        Thread.sleep(100);

        // 撤销一次
        sendCommand("undo");
        Thread.sleep(100);

        // 执行新命令
        sendCommand("test 3");
        Thread.sleep(100);

        // 尝试重做，应该提示没有可重做的操作
        sendCommand("redo");
        Thread.sleep(100);
        String output = getOutput();
        assertTrue(output.contains("Nothing to redo"),
                "Redo stack should be cleared after new command");
    }

    @Test
    public void testEmptyStackOperations() throws IOException, InterruptedException {
        // 初始化
        sendCommand("init");
        Thread.sleep(100);

        // 测试空栈撤销
        sendCommand("undo");
        Thread.sleep(100);
        String undoOutput = getOutput();
        assertTrue(undoOutput.contains("Nothing to undo"),
                "Should handle empty undo stack");

        // 测试空栈重做
        sendCommand("redo");
        Thread.sleep(100);
        String redoOutput = getOutput();
        assertTrue(redoOutput.contains("Nothing to redo"),
                "Should handle empty redo stack");
    }

    @Test
    public void testUndoRedoStateConsistency() throws IOException, InterruptedException {
        // 初始化
        sendCommand("init");
        Thread.sleep(100);

        // 记录初始状态
        sendCommand("get-context");
        Thread.sleep(100);
        String initialState = getOutput();

        // 执行命令并撤销
        sendCommand("test 1");
        Thread.sleep(100);
        sendCommand("undo");
        Thread.sleep(100);

        // 验证状态是否恢复到初始状态
        sendCommand("get-context");
        Thread.sleep(100);
        String afterUndoState = getOutput();
        assertEquals(initialState, afterUndoState,
                "State should be consistent after undo");
    }

    @Test
    public void testPrintIndentCommand() throws IOException, InterruptedException {
        // 初始化编辑器
        sendCommand("init");
        Thread.sleep(500);

        // 发送 print-indent 命令
        sendCommand("print-indent 2");
        Thread.sleep(500);
        String output = getOutput();
        System.out.println("Print Indent Output: " + output);

        // 验证输出是否符合预期
        assertTrue(output.contains("<html id=\"root\">"), "Print Indent 输出不正确");
    }

    @Test
    public void testPrintTreeCommand() throws IOException, InterruptedException {
        // 初始化编辑器
        sendCommand("init");
        Thread.sleep(500);

        // 发送 print-tree 命令
        sendCommand("print-tree");
        Thread.sleep(500);
        String output = getOutput();
        System.out.println("Print Tree Output: " + output);

        // 验证输出是否符合预期
        assertTrue(output.contains("html#root"), "Print Tree 输出不正确");
    }

    @Test
    public void testReadCommand() throws IOException {
        sendCommand("init");;
        sendCommand("read test_input.html"); //读入html文件
        HtmlElement content = HtmlContext.getInstance().getHtmlContent();

        //打印缓冲区内容
        System.out.println("Contents in buffer: " + content.toHtml());
        assertEquals(content, Files.readString(Path.of("test_input.html")),"缓冲区内容与读入文件内容不一致");
    }

    @Test
    public void testSaveCommand() throws IOException {
        sendCommand("init");
        String newContent = "<html><body><p>Modified HTML</p></body></html>";
        HtmlElement newHtml = new HtmlElement("div","root", newContent);
        HtmlContext.getInstance().setHtmlContent(newHtml);
        sendCommand("save test_output.html"); //保存html文件
        String savedContent = Files.readString(Path.of("test_output.html"));

        //打印
        System.out.println("Saved contents: " + savedContent);
        assertEquals(savedContent, newContent, "保存文件内容与缓冲区不一致");
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (editorProcess != null && editorProcess.isAlive()) {
            editorProcess.destroy();
        }
    }

    // 发送命令到编辑器进程
    private void sendCommand(String command) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(editorProcess.getOutputStream()));
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

    private BufferedReader reader;

    private void initializeReader() throws IOException {
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(editorProcess.getInputStream()));
        }
    }

    private String getOutput() throws IOException {
        initializeReader();
        StringBuilder output = new StringBuilder();
        while (reader.ready()) { // 检查是否有数据可读
            String line = reader.readLine();
            if (line != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

}
