package jira.exception;

import com.atlassian.jira.rest.client.api.domain.Issue;

import jira.issue.JiraIssue;

public class JiraIssueNotWorkableException extends JiraException {

	private static final long serialVersionUID = 1L;

	public JiraIssueNotWorkableException(String message) {
		super(message);
	}

	public JiraIssueNotWorkableException(JiraIssue issue) {
		super("Issue " + issue.getKey() + " is not workable.");
	}

	public JiraIssueNotWorkableException(Issue issue) {
		super("Issue " + issue.getKey() + " is not workable.");
	}
}
