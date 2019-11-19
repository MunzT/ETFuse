// https://stackoverflow.com/questions/4422642/java-console-jpanel
// etwas modifiziert

package de.uni_stuttgart.visus.etfuse.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class TextAreaOutputStream extends OutputStream {

    private final JTextArea textArea;
    private final StringBuilder sb = new StringBuilder();
    private String title;

    public TextAreaOutputStream(final JTextArea textArea, String title) {
        this.textArea = textArea;
        this.title = title;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public void write(int b) throws IOException {

        if (b == '\r')
            return;

        sb.append((char) b);

        if (b == '\n') {
            final String text = "["
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "]: "
                + sb.toString();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textArea.append(text);
                }
            });
            sb.setLength(0);
        }
    }
}