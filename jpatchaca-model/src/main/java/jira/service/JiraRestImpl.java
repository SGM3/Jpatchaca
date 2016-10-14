package jira.service;

import static com.google.common.collect.Iterables.toArray;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.api.domain.Session;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInput;
import com.atlassian.jira.rest.client.api.domain.input.WorklogInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import jira.JiraOptions;
import jira.exception.JiraException;
import jira.exception.JiraIssueNotFoundException;
import jira.issue.JiraAction;
import jira.issue.JiraField;
import jira.issue.JiraIssue;
import jira.issue.JiraIssueData;

public class JiraRestImpl implements Jira {

	private AsynchronousJiraRestClientFactory jiraRestClientFactory;
	private JiraOptions jiraOptions;

	public JiraRestImpl(AsynchronousJiraRestClientFactory jiraRestClientFactory, JiraOptions jiraOptions) {
		this.jiraRestClientFactory = jiraRestClientFactory;
		this.jiraOptions = jiraOptions;
	}

	@Override
	public JiraIssue getIssueByKey(String key) throws JiraException {
		Validate.notEmpty(key);
		Logger.getLogger("org.apache.axis").setLevel(Level.OFF);
		return createIssue(getIssue(key));
	}

	@Override
	public void newWorklog(String key, Calendar startDate, int timeSpent) {
		final Issue issueJira = getIssue(key);
		final WorklogInput worklogInput = createWorklogInput(issueJira, startDate, timeSpent);

		executeRequest(new Request<Void>() {
			public Promise<Void> execute(JiraRestClient client) {
				return client.getIssueClient().addWorklog(issueJira.getWorklogUri(), worklogInput);
			}
		});
	}

	@Override
	public List<JiraAction> getAvaiableActions(final JiraIssue issue) {
		Validate.notNull(issue);
		List<JiraAction> actions = new ArrayList<JiraAction>();
		final Issue issueJira = getIssue(issue.getKey());

		Iterable<Transition> transitions = executeRequest(new Request<Iterable<Transition>>() {
			public Promise<Iterable<Transition>> execute(JiraRestClient client) {
				return client.getIssueClient().getTransitions(issueJira);
			}
		});

		for (Transition transition : toArray(transitions, Transition.class)) {
			JiraAction action = new JiraAction(transition.getId(), transition.getName());
			for (Transition.Field field : toArray(transition.getFields(), Transition.Field.class))
				action.addField(new JiraField(field.getId(), getFieldName(field.getId())));

			actions.add(action);
		}

		return actions;
	}

	@Override
	public void progressWithAction(JiraIssue issue, final JiraAction action, final String comment) {
		final Issue issueJira = getIssue(issue.getKey());

		Iterable<Transition> transitions = executeRequest(new Request<Iterable<Transition>>() {
			public Promise<Iterable<Transition>> execute(JiraRestClient client) {
				return client.getIssueClient().getTransitions(issueJira);
			}
		});

		final Transition transitionAction = Iterables.find(transitions, new Predicate<Transition>() {
			public boolean apply(Transition input) {
				return input.getName().equals(action.getName());
			}
		});

		executeRequest(new Request<Void>() {
			public Promise<Void> execute(JiraRestClient client) {
				return client.getIssueClient().transition(issueJira, new TransitionInput(transitionAction.getId()));
			}
		});

		if (comment == null || comment.trim().isEmpty())
			return;

		executeRequest(new Request<Void>() {
			public Promise<Void> execute(JiraRestClient client) {
				return client.getIssueClient().addComment(issueJira.getCommentsUri(), Comment.createWithRoleLevel(comment, "Objective"));
			}
		});
	}

	@Override
	public String getIssueStatus(JiraIssue issue) {
		return getIssue(issue.getKey()).getStatus().getName();
	}

	@Override
	public String getIssueAssignee(JiraIssue issue) {
		return getIssue(issue.getKey()).getAssignee().getName();
	}

	@Override
	public void assignIssueToCurrentUser(JiraIssue issue) {
		assignIssue(issue, getCurrentUserJira());
	}

	@Override
	public void assignIssueTo(JiraIssue issue, String user) {
		assignIssue(issue, user);
	}

	@Override
	public boolean isAssignedToCurrentUser(JiraIssue issue) {
		return getIssue(issue.getKey()).getAssignee().getName().equals(getCurrentUserJira());
	}

	@Override
	public List<JiraIssue> getIssuesFromCurrentUserWithStatus(List<String> statusList) {
		List<JiraIssue> issues = new ArrayList<JiraIssue>();

		String currentUserJira = getCurrentUserJira();
		String statusJQL = StringUtils.join(statusList, ", ");
		final String jql = String.format("assignee = %s and status in (%s)", currentUserJira, statusJQL);

		SearchResult searchResult = executeRequest(new Request<SearchResult>() {
			public Promise<SearchResult> execute(JiraRestClient client) {
				return client.getSearchClient().searchJql(jql);
			}
		});

		for (Issue issue : toArray(searchResult.getIssues(), Issue.class))
			issues.add(createIssue(issue));

		return issues;
	}

	@Override
	public boolean isWorkable(JiraIssue issue) {
		if (isBrundleIssue(issue))
			return true;

		if (isOperacoesIssue(issue))
			return true;

		IssueType issueType = getIssue(issue.getKey()).getIssueType();
		return issueType.isSubtask() || issueType.getName().equals("Stand by");
	}

	private <T> T executeRequest(Request<T> request) {
		String host = jiraOptions.getURL().unbox();
		String username = jiraOptions.getUserName().unbox();
		String password = jiraOptions.getPassword().unbox();
		URI uri = createURI(host);

		JiraRestClient client = jiraRestClientFactory.createWithBasicHttpAuthentication(uri, username, password);
		try {
			return request.execute(client).get();
		} catch (InterruptedException e) {
			throw new JiraException(e.getMessage());
		} catch (ExecutionException e) {
			throw new JiraException(e.getMessage());
		} finally {
			closeClient(client);
		}
	}

	private URI createURI(String host) {
		try {
			return new URI(host);
		} catch (URISyntaxException e) {
			throw new JiraException(e.getMessage());
		}
	}

	private void closeClient(JiraRestClient client) {
		try {
			client.close();
		} catch (IOException e) {
			throw new JiraException("Error closing client connection");
		}
	}

	private Issue getIssue(final String key) {
		try {
			return executeRequest(new Request<Issue>() {
				public Promise<Issue> execute(JiraRestClient client) {
					return client.getIssueClient().getIssue(key);
				}
			});
		} catch (JiraException e) {
			throw new JiraIssueNotFoundException(key);
		}
	}

	private JiraIssue createIssue(final Issue issue) {
		final JiraIssueData data = new JiraIssueData();
		data.setId(issue.getId().toString());
		data.setKey(issue.getKey());
		data.setSummary(issue.getSummary());
		return new JiraIssue(data);
	}

	private boolean isOperacoesIssue(JiraIssue jiraIssue) {
		return jiraIssue.getKey().toLowerCase().contains("obj-");
	}

	private boolean isBrundleIssue(JiraIssue jiraIssue) {
		return jiraIssue.getKey().toLowerCase().contains("brundle");
	}

	private WorklogInput createWorklogInput(Issue issue, Calendar startDate, int timeSpent) {
		WorklogInputBuilder builder = new WorklogInputBuilder(issue.getSelf());
		builder.setStartDate(new DateTime(startDate.getTime()));
		builder.setMinutesSpent(timeSpent);
		return builder.build();
	}

	private String getFieldName(String id) {
		Iterable<Field> allFields = executeRequest(new Request<Iterable<Field>>() {
			public Promise<Iterable<Field>> execute(JiraRestClient client) {
				return client.getMetadataClient().getFields();
			}
		});

		for (Field field : toArray(allFields, Field.class))
			if (field.getId().equals(id))
				return field.getName();

		return "";
	}

	private void assignIssue(final JiraIssue issue, final String user) {
		executeRequest(new Request<Void>() {
			public Promise<Void> execute(JiraRestClient client) {
				return client.getIssueClient().updateIssue(issue.getKey(), createInputAssignIssue(user));
			}
		});
	}

	private IssueInput createInputAssignIssue(String user) {
		IssueInputBuilder builder = new IssueInputBuilder();
		builder.setAssigneeName(getCurrentUserJira());
		return builder.build();
	}

	private String getCurrentUserJira() {
		try {
			return executeRequest(new Request<Session>() {
				public Promise<Session> execute(JiraRestClient client) {
					return client.getSessionClient().getCurrentSession();
				}
			}).getUsername();
		} catch (JiraException e) {
			throw new JiraException("Error on get Session for Current User");
		}
	}

	private interface Request<T> {
		Promise<T> execute(JiraRestClient client);
	}
}