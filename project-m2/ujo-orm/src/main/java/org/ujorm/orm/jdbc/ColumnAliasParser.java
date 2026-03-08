package org.ujorm.orm.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnAliasParser {

    /** Parses column labels from the SELECT clause using Java 17+ features. */
    public List<String> parseSql(String sql) {
        var result = new ArrayList<String>();

        // Pattern to isolate the SELECT clause content
        var selectClausePattern = Pattern.compile("(?is)SELECT\\s+(.+?)\\s+FROM");
        var selectMatcher = selectClausePattern.matcher(sql);

        if (selectMatcher.find()) {
            var selectContent = selectMatcher.group(1);

            // Comprehensive pattern for column labels with various quote types
            // Group 1: '...' | Group 2: "..." | Group 3: `...` | Group 4: [...] | Group 5: plain word
            var labelPattern = Pattern.compile("(?i)\\bAS\\s+(?:'([^']+)'|\"([^\"]+)\"|`([^`]+)`|\\[([^\\]]+)\\]|(\\w+))");
            var labelMatcher = labelPattern.matcher(selectContent);

            while (labelMatcher.find()) {
                result.add(extractMatchedGroup(labelMatcher));
            }
        }

        return result;
    }

    /** Extracts the first non-null capturing group from the matcher. */
    private String extractMatchedGroup(Matcher matcher) {
        for (var i = 1; i <= matcher.groupCount(); i++) {
            if (matcher.group(i) != null) {
                return matcher.group(i);
            }
        }
        return "";
    }
}