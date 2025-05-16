package ipana.renders.ide.panels.code;

import java.util.ArrayList;

public class Code {
    private ArrayList<Line> lines;

    public Code(String... lines) {
        this.lines = new ArrayList<>();
        for (String line : lines) {
            this.lines.add(new Line(line));
        }
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void setLines(ArrayList<Line> lines) {
        this.lines = lines;
    }

    public ArrayList<String> buildToList() {
        ArrayList<String> arrayList = new ArrayList<>();
        for (Line line : getLines()) {
            arrayList.add(line.raw());
        }
        return arrayList;
    }
    public String buildToString() {
        StringBuilder builder = new StringBuilder();
        for (Line line : getLines()) {
            System.out.println(line.raw());
            builder.append(line.raw());
        }
        return builder.toString();
    }
    public ArrayList<Line> linesFromList(ArrayList<String> strings) {
        ArrayList<Line> lines = new ArrayList<>();
        for (String str : strings) {
            lines.add(new Line(str));
        }
        return lines;
    }
}
