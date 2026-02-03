package org.ujorm.tools.converter;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.ujorm.tools.web.Html;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class HtmlToJavaConverter {

    /** HTML heading pattern: h1 to h6 */
    private static final Pattern HEADING_PATTERN = Pattern.compile("^h[1-6]$");
    /** The default value of the Ujorm version */
    private static final String DEFAULT_UJORM_VERSION = "2.30";
    /** Space for indenting code. */
    private static final String OFFSET = " ".repeat(4);

    /**
     * Map of method suffixes to actual HTML tags.
     * Key: Lowercase suffix of the method name (e.g. "listitem" from "addListItem")
     * Value: The actual HTML tag (e.g. "li")
     */
    private static final Map<String, String> TAG_OVERRIDES = Map.ofEntries(
            Map.entry("anchor", Html.A),
            Map.entry("br", Html.BREAK),
            Map.entry("break", Html.BREAK),
            Map.entry("listitem", Html.LI),
            Map.entry("orderedlist", Html.OL),
            Map.entry("paragraph", Html.P),
            Map.entry("preformatted", Html.PRE),
            Map.entry("tablebody", Html.TBODY),
            Map.entry("tabledetail", Html.TD),
            Map.entry("tablehead", Html.THEAD),
            Map.entry("tablerow", Html.TR),
            Map.entry("unorderedlist", Html.UL)
    );

    /** Mapping values to HTML constants. */
    private final Map<String, String> htmlConstants = new HashMap<>();
    /** Mapping for methods without parameters (e.g. addBody()) */
    private final Map<String, String> elementNoArgMethods = new HashMap<>();
    /** Mapping for methods with varargs CharSequence (e.g. addDiv(CharSequence... classes)) */
    private final Map<String, String> elementCssMethods = new HashMap<>();
    /** Mapping for setter methods (e.g. setType(Object)) */
    private final Map<String, String> attributeSetters = new HashMap<>();

    public HtmlToJavaConverter() {
        init();
    }

    /** Initialize Reflection Data */
    private void init() {
        // 1. Mapping constants from Html.java
        for (Field field : Html.class.getDeclaredFields()) {
            if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers())
                    && field.getType().equals(String.class)) {
                try {
                    String value = (String) field.get(null);
                    htmlConstants.putIfAbsent(value, "Html." + field.getName());
                } catch (IllegalAccessException e) {
                    // Ignore inaccessible fields
                }
            }
        }

        // 2. Mapping methods from Element.java
        Class<org.ujorm.tools.web.Element> elementClass = org.ujorm.tools.web.Element.class;
        for (Method method : elementClass.getMethods()) {
            String name = method.getName();
            Class<?>[] paramTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();
            if (!returnType.equals(elementClass)) {
                continue;
            }

            // 2.a Mapping Adders: addDiv(...)
            if (name.startsWith("add") && name.length() > 3) {
                String rawSuffix = name.substring(3).toLowerCase();
                // Apply overrides from the Map (e.g. "listitem" -> "li")
                String tagName = TAG_OVERRIDES.getOrDefault(rawSuffix, rawSuffix);

                if (paramTypes.length == 0) {
                    elementNoArgMethods.put(tagName, name);
                } else if (paramTypes.length == 1
                        && paramTypes[0].isArray()
                        && CharSequence.class.isAssignableFrom(paramTypes[0].getComponentType())) {
                    elementCssMethods.put(tagName, name);
                }
            }

            // 2.b Mapping Setters: setType("...") -> type
            if (name.startsWith("set") && name.length() > 3 && paramTypes.length == 1) {
                Class<?> type = paramTypes[0];
                // Strict type check to ensure we can pass a String literal safely
                if (type == CharSequence.class || type == String.class || type == Object.class) {
                    String attrName = name.substring(3).toLowerCase();
                    attributeSetters.put(attrName, name);
                }
            }
        }
    }

    /** Convert a HTML code to the Java code with Element classes. */
    public String convertHtmlToJavaElements(String htmlContent) throws IOException {
        return convertHtmlToJavaElements(htmlContent, false);
    }

    /** Convert a HTML code to the Java code with Element classes. */
    public String convertHtmlToJavaElements(String htmlContent, boolean blockStyle) throws IOException {
        Appendable writer = new StringWriter();
        convertHtmlToJavaElements(htmlContent, blockStyle, writer);
        return writer.toString();
    }

    /** Convert a HTML code to the Java code with Element classes. */
    public void convertHtmlToJavaElements(String htmlContent, boolean blockStyle, Appendable writer) throws IOException {
        final Document doc = Jsoup.parse(htmlContent);
        final String docTitle = getAndRemoveTitle(doc);
        final Element root = doc.child(0); // typically <html>

        // Added JavaDoc with version
        String version = Html.class.getPackage().getImplementationVersion();
        writer.append("/** Generated for use with: org.ujorm:ujo-web:")
                .append(version != null ? version : DEFAULT_UJORM_VERSION)
                .append(" (").append(LocalDate.now().toString()).append(") */\n")
                .append("public String htmlGenerator() {\n")
                .append(OFFSET).append("var result = HttpContext.of();\n")
                .append(OFFSET).append("try (var html = HtmlElement.niceOf(\"")
                .append(escapeJavaString(docTitle))
                .append("\", result)) {\n");

        writeAttributes(root, writer, OFFSET.repeat(2) + "html", ");\n", Set.of());

        Deque<String> ancestors = new ArrayDeque<>();
        for (Node child : root.childNodes()) {
            writeRecursive(child, "html", writer, 2, ancestors, blockStyle);
        }

        writer.append(OFFSET).append("}\n")
                .append(OFFSET).append("return result.toString();\n")
                .append("}");
    }

    private void writeRecursive(Node node,
                                String parentVar,
                                Appendable writer,
                                int depth,
                                Deque<String> ancestors,
                                boolean blockStyle) throws IOException {
        final String indent = OFFSET.repeat(depth);

        // 1. Handle Text and Data nodes (Leafs)
        if (node instanceof TextNode textNode) {
            writeTextCommon(writer, indent, parentVar, ");\n", "addText", textNode.getWholeText());
            return;
        } else if (node instanceof DataNode dataNode) {
            writeTextCommon(writer, indent, parentVar, ");\n", "addRawText", dataNode.getWholeData());
            return;
        }

        // 2. Handle Elements
        if (node instanceof Element element) {
            if (isEmptyHead(element)) {
                return;
            }
            String tagName = element.tagName();
            CreationResult creation = resolveCreationCode(parentVar, element);

            // Check if element has any child Elements (recursive branches)
            boolean hasElementChildren = element.childNodes().stream().anyMatch(n -> n instanceof Element);
            boolean useBlock = blockStyle || hasElementChildren;

            if (useBlock) {
                // BLOCK STYLE: try (var x = ...) { ... }
                String currentVar = generateVariableName(tagName, ancestors);
                writer.append(indent).append("try (var ").append(currentVar).append(" = ").append(creation.code).append(") {\n");
                writeAttributes(element, writer, OFFSET.repeat(depth + 1) + currentVar, ");\n", creation.consumedAttributes);

                ancestors.push(tagName);
                for (Node child : element.childNodes()) {
                    writeRecursive(child, currentVar, writer, depth + 1, ancestors, blockStyle);
                }
                ancestors.pop();
                writer.append(indent).append("}\n");
            } else {
                // CHAIN STYLE: parent.addDiv().setAttribute(...).addText(...);
                writer.append(indent).append(creation.code);
                String chainedIndent = indent + OFFSET;
                String chainPrefix = "\n" + chainedIndent;

                // Chain attributes
                writeAttributes(element, writer, chainPrefix, ")", creation.consumedAttributes);

                // Chain text content (we know there are no Element children)
                for (Node child : element.childNodes()) {
                    if (child instanceof TextNode t) {
                        writeTextCommon(writer, chainedIndent, chainPrefix, ")", "addText", t.getWholeText());
                    } else if (child instanceof DataNode d) {
                        writeTextCommon(writer, chainedIndent, chainPrefix, ")", "addRawText", d.getWholeData());
                    }
                }
                writer.append(";\n");
            }
        }
    }

    /**
     * Common logic to write text nodes in both Block and Chained styles.
     * @param prefix For block: "indent + var"; For chain: "\n + indent" (WITHOUT DOT)
     * @param suffix For block: ");\n"; For chain: ")"
     */
    private void writeTextCommon(Appendable writer, String indent, String prefix, String suffix, String method, String text) throws IOException {
        if (text.isBlank()) return;

        // Adjust prefix for chained calls if it starts with newline
        String callPrefix = prefix.startsWith("\n") ? prefix : indent + prefix;
        printTextCall(writer, callPrefix + ".", method, text, indent);
        writer.append(suffix);
    }

    /**
     * Common logic for printing the method call and its argument content.
     * Does NOT print the closing parenthesis ')'.
     */
    private void printTextCall(Appendable writer, String prefix, String method, String text, String indent) throws IOException {
        writer.append(prefix).append(method).append("(");

        if (text.contains("\n")) {
            String blockIndent = indent + OFFSET;
            writeMultilineContent(writer, text, blockIndent);
        } else {
            String nextLineIndent = indent + OFFSET + (prefix.startsWith("\n") ? "" : OFFSET);
            // Single line string
            writer.append("\"")
                    .append(escapeJavaText(text, nextLineIndent))
                    .append("\"");
        }
    }

    /**
     * Shared logic for writing multiline text content.
     * Handles "\n" + indentation strategy.
     * Extracts ALL leading newlines to avoid empty lines inside the Text Block.
     */
    private void writeMultilineContent(Appendable writer, String input, String blockIndent) throws IOException {
        // Normalize line endings first to ensure consistency
        String text = input.replace("\r\n", "\n").replace("\r", "\n");

        int leadingNewLines = 0;
        while (leadingNewLines < text.length() && text.charAt(leadingNewLines) == '\n') {
            leadingNewLines++;
        }
        if (leadingNewLines == text.length()) {
            writer.append("\"").append("\\n".repeat(leadingNewLines)).append("\"");
            return;
        }
        String body = text.substring(leadingNewLines);
        if (leadingNewLines > 0) {
            writer.append("\"").append("\\n".repeat(leadingNewLines)).append("\" +");
            writer.append("\n").append(blockIndent);
        } else {
            writer.append("\n").append(blockIndent);
        }

        writer.append("\"\"\"\n");
        writer.append(escapeJavaTextBlock(body, blockIndent));

        if (body.endsWith("\n")) {
            writer.append(blockIndent).append("\"\"\"");
        } else {
            writer.append("\\\n").append(blockIndent).append("\"\"\"");
        }
    }

    /** Get TITLE, defaults to "Demo" if blank, and remove it from the doc */
    private static @NotNull String getAndRemoveTitle(Document document) {
        String result = document.title();
        document.select(Html.TITLE).remove();
        return result.isBlank() ? "Demo" : result;
    }

    /** Check if the element is an empty HEAD tag (no attributes, no content). */
    private boolean isEmptyHead(Element element) {
        return Html.HEAD.equals(element.tagName())
                && element.attributes().isEmpty()
                && element.childNodes().stream().noneMatch(this::isElementOrTextContent);
    }

    /** Check if the node contains non-blank text */
    private boolean isElementOrTextContent(final Node node) {
        if (node instanceof Element) return true;
        if (node instanceof TextNode t) {
            return !t.getWholeText().isBlank();
        }
        if (node instanceof DataNode d) {
            return !d.getWholeData().isBlank();
        }
        return false;
    }

    /** Helper record for the result of generating creation code. */
    private record CreationResult(String code, Set<String> consumedAttributes) {}

    private CreationResult resolveCreationCode(String parentVar, Element element) {
        String tagName = element.tagName();
        String classValue = element.attr("class");
        boolean hasClass = !classValue.isBlank();

        if (HEADING_PATTERN.matcher(tagName).matches()) {
            int level = Integer.parseInt(tagName.substring(1));
            String args = hasClass ? level + ", " + formatCssArgs(classValue) : String.valueOf(level);
            return createResult(parentVar, "addHeadingX", args, Set.of("class"));
        }

        // 2. Handle Anchors (a) -> addAnchor(url, cssClasses)
        if (Html.A.equals(tagName) && element.hasAttr(Html.A_HREF)) {
            String url = element.attr("href");
            StringBuilder args = new StringBuilder().append('"').append(escapeJavaString(url)).append('"');
            if (hasClass) {
                args.append(", ").append(formatCssArgs(classValue));
            }
            return createResult(parentVar, "addAnchor", args.toString(), Set.of("href", "class"));
        }

        // 3. Standard resolution based on reflection maps
        if (hasClass && elementCssMethods.containsKey(tagName)) {
            return createResult(parentVar, elementCssMethods.get(tagName), formatCssArgs(classValue), Set.of("class"));
        }
        if (elementNoArgMethods.containsKey(tagName)) {
            return createResult(parentVar, elementNoArgMethods.get(tagName), "", Collections.emptySet());
        }
        if (elementCssMethods.containsKey(tagName)) {
            return createResult(parentVar, elementCssMethods.get(tagName), "", Collections.emptySet());
        }

        // 4. Generic addElement
        String nameArg = htmlConstants.getOrDefault(tagName, "\"" + tagName + "\"");
        if (hasClass) {
            return createResult(parentVar, "addElement", nameArg + ", " + formatCssArgs(classValue), Set.of("class"));
        } else {
            return createResult(parentVar, "addElement", nameArg, Collections.emptySet());
        }
    }

    /** Helper to reduce duplication in resolveCreationCode */
    private CreationResult createResult(String parent, String method, String args, Set<String> consumed) {
        String code = parent + "." + method + "(" + args + ")";
        return new CreationResult(code, consumed);
    }

    /**
     * Splits class string by whitespace and formats as comma-separated quoted strings.
     * Example: "foo bar" -> "foo", "bar"
     */
    private String formatCssArgs(String classValue) {
        return Arrays.stream(classValue.split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(s -> "\"" + escapeJavaString(s) + "\"")
                .collect(Collectors.joining(", "));
    }

    private String generateVariableName(String tagName, Deque<String> ancestors) {
        String lowerTag = tagName.toLowerCase();
        String baseName = lowerTag.contains("-") ? toCamelCase(lowerTag) : lowerTag;

        if (HEADING_PATTERN.matcher(baseName).matches()) {
            return baseName;
        }

        long count = ancestors.stream()
                .filter(t -> t.equalsIgnoreCase(tagName))
                .count();

        return (count > 0) ? baseName + (count + 1) : baseName;
    }

    private String toCamelCase(String input) {
        String[] parts = input.split("-");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (!parts[i].isEmpty()) {
                sb.append(Character.toUpperCase(parts[i].charAt(0)));
                sb.append(parts[i].substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Unified method to write attributes for both block and chained styles.
     * @param prefix String prepended to the setAttribute call. MUST NOT include the separator (dot).
     * @param suffix String appended to the call (e.g. ");\n" or ")")
     */
    private void writeAttributes(Element element, Appendable writer, String prefix, String suffix, Set<String> ignoreKeys) throws IOException {
        if (element.attributes().isEmpty()) return;

        for (Attribute attr : element.attributes()) {
            String key = attr.getKey();
            if (ignoreKeys.contains(key)) {
                continue;
            }

            String setterName = attributeSetters.get(key);
            if (setterName != null) {
                // Short version: .setType("value")
                // FIX: Remove the leading ')' from suffix because the specific setter closes itself.
                String setterSuffix = suffix.startsWith(")") ? suffix.substring(1) : suffix;

                writer.append(prefix)
                        .append(".").append(setterName).append("(\"")
                        .append(escapeJavaString(attr.getValue()))
                        .append("\")")
                        .append(setterSuffix);
            } else {
                // Standard version: .setAttribute(Html.A_TYPE, "value")
                String attrCode = htmlConstants.getOrDefault(key, "\"" + key + "\"");
                writer.append(prefix)
                        .append(".setAttribute(")
                        .append(attrCode)
                        .append(", \"")
                        .append(escapeJavaString(attr.getValue()))
                        .append("\"")
                        .append(suffix);
            }
        }
    }

    /**
     * Escapes characters for Java string.
     * Note: This method escapes \n to \\n (keeping it as a single line string).
     * Used mainly for attributes.
     */
    private String escapeJavaString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Escapes text content and physically breaks Java code lines on newline characters.
     * Used for single line strings that need concatenation.
     */
    private String escapeJavaText(String input, String nextLineIndent) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        // Standardize line endings just in case
        String text = input.replace("\r\n", "\n").replace("\r", "\n");

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\t' -> sb.append("\\t");
                case '\n' -> sb.append("\\n\"\n")
                        .append(nextLineIndent)
                        .append("+ \"");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Escapes text content for Java Text Blocks (""" ... """).
     * Indents the content so it aligns with the generated code.
     */
    private String escapeJavaTextBlock(String input, String indent) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        String text = input.replace("\r\n", "\n").replace("\r", "\n");

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '\t' -> sb.append("\\t");
                case '"' -> {
                    if (i + 2 < text.length() && text.charAt(i + 1) == '"' && text.charAt(i + 2) == '"') {
                        sb.append("\\\"");
                    } else {
                        sb.append("\"");
                    }
                }
                default -> sb.append(c);
            }
        }

        // Add indentation to all lines
        String result = indent + sb.toString().replace("\n", "\n" + indent);

        // IMPORTANT: If the text ends with a newline, we do NOT want the indentation
        // on the very last empty line (where the closing """ will be).
        if (result.endsWith("\n" + indent)) {
            return result.substring(0, result.length() - indent.length());
        }
        return result;
    }
}