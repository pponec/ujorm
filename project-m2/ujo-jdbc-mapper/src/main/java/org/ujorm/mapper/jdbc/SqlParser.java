package org.ujorm.mapper.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlParser {

    /** Parses column aliases from the SELECT clause using Java 17+ features. */
    public List<String> parseSql(String sql) {
        var result = new ArrayList<String>();

        // Pattern to isolate the SELECT clause content
        var selectClausePattern = Pattern.compile("(?is)SELECT\\s+(.+?)\\s+FROM");
        var selectMatcher = selectClausePattern.matcher(sql);

        if (selectMatcher.find()) {
            var selectContent = selectMatcher.group(1);

            // Comprehensive pattern for aliases with various quote types
            // Group 1: '...' | Group 2: "..." | Group 3: `...` | Group 4: [...] | Group 5: plain word
            var aliasPattern = Pattern.compile("(?i)\\bAS\\s+(?:'([^']+)'|\"([^\"]+)\"|`([^`]+)`|\\[([^\\]]+)\\]|(\\w+))");
            var aliasMatcher = aliasPattern.matcher(selectContent);

            while (aliasMatcher.find()) {
                result.add(extractMatchedGroup(aliasMatcher));
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