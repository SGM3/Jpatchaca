package jira;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;

import jira.issue.JiraIssue;
import jira.service.Jira;
import jira.service.JiraRestImpl;
import lang.Maybe;

@RunWith(MockitoJUnitRunner.class)
public class JiraTest {

	private Jira subject;
	@Mock
	private AsynchronousJiraRestClientFactory jiraRestClientFactory;
	@Mock
	private JiraOptions jiraOptions;
	@Mock
	private JiraRestClient jiraRestClient;
	@Mock
	private IssueRestClient issueClient;
	@Mock
	private Promise<Issue> promiseIssue;
	@Mock
	private Issue issue;

	@Before
	public void setUp() throws URISyntaxException {
		subject = new JiraRestImpl(jiraRestClientFactory, jiraOptions);

		when(jiraOptions.getUserName()).thenReturn(Maybe.wrap("teste"));
		when(jiraOptions.getPassword()).thenReturn(Maybe.wrap("teste"));
		when(jiraOptions.getURL()).thenReturn(Maybe.wrap("localhost"));

		when(jiraRestClientFactory.createWithBasicHttpAuthentication(new URI("localhost"), "teste", "teste")).thenReturn(jiraRestClient);
		when(jiraRestClient.getIssueClient()).thenReturn(issueClient);
	}

	@Test
	public void getIssueByKey() throws InterruptedException, ExecutionException {
		mockIssue(1L, "ISSUE-1", "TESTE");
		JiraIssue issue = subject.getIssueByKey("ISSUE-1");
		assertIssue(issue, "1", "ISSUE-1", "TESTE");
	}

	private void assertIssue(JiraIssue issue, String id, String key, String summary) {
		assertEquals(issue.getId(), id);
		assertEquals(issue.getKey(), key);
		assertEquals(issue.getSummary(), summary);
	}

	private void mockIssue(long id, String key, String summary) throws InterruptedException, ExecutionException {
		when(issueClient.getIssue(key)).thenReturn(promiseIssue);
		when(promiseIssue.get()).thenReturn(issue);

		when(issue.getId()).thenReturn(id);
		when(issue.getKey()).thenReturn(key);
		when(issue.getSummary()).thenReturn(summary);
	}
}
