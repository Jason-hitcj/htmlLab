package Commands;

import Utils.*;

public class UndoCommand implements Command {
    @Override
    public void execute() {
        HtmlHistory.getInstance().undo();
    }
}
