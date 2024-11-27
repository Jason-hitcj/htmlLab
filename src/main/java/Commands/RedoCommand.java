package Commands;

import Utils.*;

public class RedoCommand implements Command {
    @Override
    public void execute() {
        HtmlHistory.getInstance().redo();
    }
}
