
package org.owasp.webgoat.plugin.introduction;

import org.owasp.webgoat.assignments.AssignmentEndpoint;
import org.owasp.webgoat.assignments.AssignmentHints;
import org.owasp.webgoat.assignments.AssignmentPath;
import org.owasp.webgoat.assignments.AttackResult;
import org.owasp.webgoat.session.DatabaseUtilities;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.*;

@AssignmentPath("/SqlInjection/attack10")
@AssignmentHints(value = {"SqlStringInjectionHint10-1", "SqlStringInjectionHint10-2", "SqlStringInjectionHint10-3", "SqlStringInjectionHint10-4", "SqlStringInjectionHint10-5", "SqlStringInjectionHint10-6"})
public class SqlInjectionLesson10 extends AssignmentEndpoint {

    @RequestMapping(method = RequestMethod.POST)
    public
    @ResponseBody
    AttackResult completed(@RequestParam String action) {
        return injectableQueryAvailability(action);
    }

    protected AttackResult injectableQueryAvailability(String action) {
        StringBuffer output = new StringBuffer();
        String query = "SELECT * FROM access_log WHERE action LIKE '%" + action + "%'";

        try {
            Connection connection = DatabaseUtilities.getConnection(getWebSession());

            try {
                Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet results = statement.executeQuery(query);

                if (results.getStatement() != null && results.first()) {
                    output.append(SqlInjectionLesson8.generateTable(results));
                    results.last();
                    return trackProgress(failed().feedback("sql-injection.10.entries").output(output.toString()).build());
                } else {
                    if (tableExists(connection)) {
                        return trackProgress(failed().output(output.toString()).build());
                    }
                    else {
                        return trackProgress(success().feedback("sql-injection.10.success").build());
                    }
                }
            } catch (SQLException e) {
                if (tableExists(connection)) {
                    return trackProgress(failed().output("<span class='feedback-negative'>" + e.getMessage() + "</span><br>" + output.toString()).build());
                }
                else {
                    return trackProgress(success().feedback("sql-injection.10.success").build());
                }
            }

        } catch (Exception e) {
            return trackProgress(failed().output("<span class='feedback-negative'>" + e.getMessage() + "</span>").build());
        }
    }

    private boolean tableExists(Connection connection) throws SQLException {
        ResultSet res = connection.getMetaData().getTables(null, null, "access_log", null);
        while (res.next()) {
            String table_name = res.getString("TABLE_NAME");
            if (table_name != null && table_name.equals("access_log")) {
                return true;
            }
        }
        return false;
    }

}
