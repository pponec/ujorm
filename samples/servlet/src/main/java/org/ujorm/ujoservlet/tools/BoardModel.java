package org.ujorm.ujoservlet.tools;

import java.util.Base64;
import java.util.BitSet;
import java.util.Optional;
import org.ujorm.tools.Check;

/**
 * The Game board
 * @author Pavel Ponec
 */
public class BoardModel {

    /** A width of the board */
    private final int width;
    /** A heigt of the board */
    private final int height;
    /** Internal felds */
    private final BitSet fields;
    /** Data compressor */
    private final Compressor compressor = new Compressor();
    /** Optional error message */
    private String errorMessage;

    /** A constructor
     * @param width A width of the board
     * @param height a heigh of th board
     * @param content A optional content from the method {@link #exportBoard() }
     */
    public BoardModel(int width, int height, String content) {
        this.width = width;
        this.height = height;
        if (Check.hasLength(content)) {
            final byte[] data = compressor.decompress(Base64.getUrlDecoder().decode(content));
            this.fields = BitSet.valueOf(data);
        } else {
            this.fields = new BitSet(width * height);
            System.out.println("fields.length " + fields.length());
        }
    }

    public void setStone(int i) {
        fields.set(i);
    }

    public void setStone(int x, int y) {
        fields.set(y * width + x);
    }

    /** Get a field state */
    public boolean isStone(int x, int y) {
        return fields.get(y * width + x);
    }

    /** A board width */
    public int getWidth() {
        return width;
    }

    /** A board height */
    public int getHeight() {
        return height;
    }

    /** Export board content to a String format */
    public String exportBoard() {
        return Base64.getUrlEncoder().encodeToString(compressor.compress(fields.toByteArray()))
                .replace('=', ' ')
                .trim();
    }

    /** Set a error message */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /** Get an error message */
    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

}
