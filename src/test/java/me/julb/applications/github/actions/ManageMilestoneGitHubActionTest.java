/**
 * MIT License
 *
 * Copyright (c) 2017-2022 Julb
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.julb.applications.github.actions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHMilestoneState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.LocalPagedIterable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import me.julb.sdk.github.actions.kit.GitHubActionsKit;

/**
 * Test class for {@link ManageMilestoneGitHubAction} class. <br>
 * @author Julb.
 */
@ExtendWith(MockitoExtension.class)
class ManageMilestoneGitHubActionTest {

    /**
     * The class under test.
     */
    private ManageMilestoneGitHubAction githubAction = null;

    /**
     * A mock for GitHub action kit.
     */
    @Mock
    private GitHubActionsKit ghActionsKitMock;

    /**
     * A mock for GitHub API.
     */
    @Mock
    private GitHub ghApiMock;

    /**
     * A mock for GitHub repository.
     */
    @Mock
    private GHRepository ghRepositoryMock;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp()
        throws Exception {
        githubAction = new ManageMilestoneGitHubAction();
        githubAction.setGhActionsKit(ghActionsKitMock);
        githubAction.setGhApi(ghApiMock);
        githubAction.setGhRepository(ghRepositoryMock);
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputTitle_thenReturnValue()
        throws Exception {
        when(this.ghActionsKitMock.getRequiredInput("title")).thenReturn("v1.0.0");

        assertThat(this.githubAction.getInputTitle()).isEqualTo("v1.0.0");

        verify(this.ghActionsKitMock).getRequiredInput("title");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputTitleNotProvided_thenFail() {
        when(this.ghActionsKitMock.getRequiredInput("title")).thenThrow(NoSuchElementException.class);
        assertThrows(CompletionException.class, () -> this.githubAction.execute());
        verify(this.ghActionsKitMock).getRequiredInput("title");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputStateProvided_thenReturnValue()
        throws Exception {
        when(this.ghActionsKitMock.getEnumInput("state", InputMilestoneState.class)).thenReturn(Optional.of(InputMilestoneState.CLOSED));

        assertThat(this.githubAction.getInputState()).isEqualTo(InputMilestoneState.CLOSED);

        verify(this.ghActionsKitMock).getEnumInput("state", InputMilestoneState.class);
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputStateNotProvided_thenReturnDefaultValue()
        throws Exception {
        when(this.ghActionsKitMock.getEnumInput("state", InputMilestoneState.class)).thenReturn(Optional.empty());

        assertThat(this.githubAction.getInputState()).isEqualTo(InputMilestoneState.OPEN);

        verify(this.ghActionsKitMock).getEnumInput("state", InputMilestoneState.class);
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputDescriptionPresent_thenReturnValue()
        throws Exception {
        when(this.ghActionsKitMock.getInput("description")).thenReturn(Optional.of("some description"));

        assertThat(this.githubAction.getInputDescription()).isEqualTo(Optional.of("some description"));

        verify(this.ghActionsKitMock).getInput("description");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputDescriptionEmpty_thenReturnEmpty()
        throws Exception {
        when(this.ghActionsKitMock.getInput("description")).thenReturn(Optional.empty());

        assertThat(this.githubAction.getInputDescription()).isEmpty();

        verify(this.ghActionsKitMock).getInput("description");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputDueOnPresent_thenReturnValue()
        throws Exception {
        when(this.ghActionsKitMock.getInput("due_on")).thenReturn(Optional.of("2022-01-01"));

        // Should return the date set to 8am
        var cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(Calendar.YEAR, 2022);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        assertThat(this.githubAction.getInputDueOn()).isPresent().contains(cal.getTime());

        verify(this.ghActionsKitMock).getInput("due_on");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputDueOnEmpty_thenReturnEmpty()
        throws Exception {
        when(this.ghActionsKitMock.getInput("due_on")).thenReturn(Optional.empty());

        assertThat(this.githubAction.getInputDueOn()).isEmpty();

        verify(this.ghActionsKitMock).getInput("due_on");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputDueOnInvalid_thenThrowIllegalArgumentException()
        throws Exception {
        when(this.ghActionsKitMock.getInput("due_on")).thenReturn(Optional.of("abcd"));

        assertThrows(IllegalArgumentException.class, () -> this.githubAction.getInputDueOn());

        verify(this.ghActionsKitMock).getInput("due_on");
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteOpenMilestoneExists_thenMilestoneOpened()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghMilestoneExisting = Mockito.mock(GHMilestone.class);
        when(ghMilestoneExisting.getNumber()).thenReturn(123);

        var dueOn = new Date();

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("v1.0.0").when(spy).getInputTitle();
        doReturn(InputMilestoneState.OPEN).when(spy).getInputState();
        doReturn(Optional.of("description")).when(spy).getInputDescription();
        doReturn(Optional.of(dueOn)).when(spy).getInputDueOn();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.of(ghMilestoneExisting)).when(spy).getGHMilestone("v1.0.0");
        doReturn(ghMilestoneExisting).when(spy).createGHMilestone("v1.0.0", GHMilestoneState.OPEN, Optional.of("description"), Optional.of(dueOn), Optional.of(ghMilestoneExisting));

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputTitle();
        verify(spy).getInputState();
        verify(spy).getInputDescription();
        verify(spy).getInputDueOn();
        verify(spy).connectApi();
        verify(spy).getGHMilestone("v1.0.0");
        verify(spy).createGHMilestone("v1.0.0", GHMilestoneState.OPEN, Optional.of("description"), Optional.of(dueOn), Optional.of(ghMilestoneExisting));

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NUMBER.key(), 123);
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteOpenMilestoneNotExists_thenMilestoneOpened()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghMilestoneCreated = Mockito.mock(GHMilestone.class);
        when(ghMilestoneCreated.getNumber()).thenReturn(123);

        var dueOn = new Date();

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("v1.0.0").when(spy).getInputTitle();
        doReturn(InputMilestoneState.OPEN).when(spy).getInputState();
        doReturn(Optional.of("description")).when(spy).getInputDescription();
        doReturn(Optional.of(dueOn)).when(spy).getInputDueOn();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.empty()).when(spy).getGHMilestone("v1.0.0");
        doReturn(ghMilestoneCreated).when(spy).createGHMilestone("v1.0.0", GHMilestoneState.OPEN, Optional.of("description"), Optional.of(dueOn), Optional.empty());

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputTitle();
        verify(spy).getInputState();
        verify(spy).getInputDescription();
        verify(spy).getInputDueOn();
        verify(spy).connectApi();
        verify(spy).getGHMilestone("v1.0.0");
        verify(spy).createGHMilestone("v1.0.0", GHMilestoneState.OPEN, Optional.of("description"), Optional.of(dueOn), Optional.empty());

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NUMBER.key(), 123);
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteCloseMilestoneExists_thenMilestoneClosed()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghMilestoneExisting = Mockito.mock(GHMilestone.class);
        when(ghMilestoneExisting.getNumber()).thenReturn(123);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("v1.0.0").when(spy).getInputTitle();
        doReturn(InputMilestoneState.CLOSED).when(spy).getInputState();
        doReturn(Optional.empty()).when(spy).getInputDescription();
        doReturn(Optional.empty()).when(spy).getInputDueOn();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.of(ghMilestoneExisting)).when(spy).getGHMilestone("v1.0.0");
        doReturn(ghMilestoneExisting).when(spy).createGHMilestone("v1.0.0", GHMilestoneState.CLOSED, Optional.empty(), Optional.empty(), Optional.of(ghMilestoneExisting));

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputTitle();
        verify(spy).getInputState();
        verify(spy).getInputDescription();
        verify(spy).getInputDueOn();
        verify(spy).connectApi();
        verify(spy).getGHMilestone("v1.0.0");
        verify(spy).createGHMilestone("v1.0.0", GHMilestoneState.CLOSED, Optional.empty(), Optional.empty(), Optional.of(ghMilestoneExisting));

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NUMBER.key(), 123);
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteCloseMilestoneNotExists_thenMilestoneClosed()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghMilestoneCreated = Mockito.mock(GHMilestone.class);
        when(ghMilestoneCreated.getNumber()).thenReturn(123);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("v1.0.0").when(spy).getInputTitle();
        doReturn(InputMilestoneState.CLOSED).when(spy).getInputState();
        doReturn(Optional.empty()).when(spy).getInputDescription();
        doReturn(Optional.empty()).when(spy).getInputDueOn();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.empty()).when(spy).getGHMilestone("v1.0.0");
        doReturn(ghMilestoneCreated).when(spy).createGHMilestone("v1.0.0", GHMilestoneState.CLOSED, Optional.empty(), Optional.empty(), Optional.empty());

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputTitle();
        verify(spy).getInputState();
        verify(spy).getInputDescription();
        verify(spy).getInputDueOn();
        verify(spy).connectApi();
        verify(spy).getGHMilestone("v1.0.0");
        verify(spy).createGHMilestone("v1.0.0", GHMilestoneState.CLOSED, Optional.empty(), Optional.empty(), Optional.empty());

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NUMBER.key(), 123);
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteDeleteMilestoneExists_thenMilestoneDeleted()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghMilestoneExisting = Mockito.mock(GHMilestone.class);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("v1.0.0").when(spy).getInputTitle();
        doReturn(InputMilestoneState.DELETED).when(spy).getInputState();
        doReturn(Optional.empty()).when(spy).getInputDescription();
        doReturn(Optional.empty()).when(spy).getInputDueOn();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.of(ghMilestoneExisting)).when(spy).getGHMilestone("v1.0.0");
        doNothing().when(spy).deleteGHMilestone(Optional.of(ghMilestoneExisting));

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputTitle();
        verify(spy).getInputState();
        verify(spy).getInputDescription();
        verify(spy).getInputDueOn();
        verify(spy).connectApi();
        verify(spy).getGHMilestone("v1.0.0");
        verify(spy).deleteGHMilestone(Optional.of(ghMilestoneExisting));

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock).setEmptyOutput(OutputVars.NUMBER.key());
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteDeleteMilestoneNotExists_thenMilestoneDeleted()
        throws Exception {
        var spy = spy(this.githubAction);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn("v1.0.0").when(spy).getInputTitle();
        doReturn(InputMilestoneState.DELETED).when(spy).getInputState();
        doReturn(Optional.empty()).when(spy).getInputDescription();
        doReturn(Optional.empty()).when(spy).getInputDueOn();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Optional.empty()).when(spy).getGHMilestone("v1.0.0");
        doNothing().when(spy).deleteGHMilestone(Optional.empty());

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputTitle();
        verify(spy).getInputState();
        verify(spy).getInputDescription();
        verify(spy).getInputDueOn();
        verify(spy).connectApi();
        verify(spy).getGHMilestone("v1.0.0");
        verify(spy).deleteGHMilestone(Optional.empty());

        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock).setEmptyOutput(OutputVars.NUMBER.key());
    }

    /**
     * Test method.
     */
    @Test
    void whenConnectApi_thenVerifyOK()
        throws Exception {
        when(ghActionsKitMock.getRequiredEnv("GITHUB_TOKEN")).thenReturn("token");
        when(ghActionsKitMock.getGitHubApiUrl()).thenReturn("https://api.github.com");

        this.githubAction.connectApi();

        verify(ghActionsKitMock).getRequiredEnv("GITHUB_TOKEN");
        verify(ghActionsKitMock).getGitHubApiUrl();
        verify(ghActionsKitMock, times(2)).debug(Mockito.anyString());
        verify(ghApiMock).checkApiUrlValidity();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetGHMilestoneExist_thenReturnRef()
        throws Exception {
        var ghMilestone1 = Mockito.mock(GHMilestone.class);
        when(ghMilestone1.getTitle()).thenReturn("v0.0.0");

        var ghMilestone2 = Mockito.mock(GHMilestone.class);
        when(ghMilestone2.getTitle()).thenReturn("v1.0.0");

        when(ghRepositoryMock.listMilestones(GHIssueState.ALL)).thenReturn(new LocalPagedIterable<>(List.of(ghMilestone1, ghMilestone2)));

        assertThat(this.githubAction.getGHMilestone("v1.0.0")).isPresent().contains(ghMilestone2);

        verify(ghRepositoryMock).listMilestones(GHIssueState.ALL);
        verify(ghMilestone1).getTitle();
        verify(ghMilestone2).getTitle();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetGHMilestoneDoesNotExist_thenReturnEmpty()
        throws Exception {
        when(ghRepositoryMock.listMilestones(GHIssueState.ALL)).thenReturn(new LocalPagedIterable<>(List.of()));

        assertThat(this.githubAction.getGHMilestone("v1.0.0")).isEmpty();

        verify(ghRepositoryMock).listMilestones(GHIssueState.ALL);
    }

    /**
     * Test method.
     */
    @Test
    void whenGetGHMilestoneNull_thenThrowNullPointerException()
        throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.getGHMilestone(null));
    }

    /**
     * Test method.
     */
    @Test
    void whenCreateGHMilestoneOpenEmptyMilestone_thenOpenMilestone()
        throws Exception {
        var ghMilestoneMock = mock(GHMilestone.class);
        when(ghRepositoryMock.createMilestone("v1.0.0", null)).thenReturn(ghMilestoneMock);
        when(ghMilestoneMock.getState()).thenReturn(GHMilestoneState.OPEN);

        this.githubAction.createGHMilestone("v1.0.0", GHMilestoneState.OPEN, Optional.empty(), Optional.empty(), Optional.empty());

        verify(ghActionsKitMock).notice(Mockito.anyString());
        verify(ghRepositoryMock).createMilestone("v1.0.0", null);
        verify(ghMilestoneMock, never()).reopen();
        verify(ghMilestoneMock, never()).close();
    }

    /**
     * Test method.
     */
    @Test
    void whenCreateGHMilestoneOpenExistingOpenMilestone_thenOpenMilestone()
        throws Exception {
        var ghMilestoneMock = mock(GHMilestone.class);
        when(ghMilestoneMock.getState()).thenReturn(GHMilestoneState.OPEN);

        var dueOn = new Date();

        this.githubAction.createGHMilestone("v1.0.0", GHMilestoneState.OPEN, Optional.of("description"), Optional.of(dueOn), Optional.of(ghMilestoneMock));

        verify(ghActionsKitMock).notice(Mockito.anyString());
        verify(ghMilestoneMock).setDueOn(dueOn);
        verify(ghMilestoneMock).setDescription("description");
        verify(ghMilestoneMock, never()).reopen();
        verify(ghMilestoneMock, never()).close();
    }

    /**
     * Test method.
     */
    @Test
    void whenCreateGHMilestoneCloseExistingOpenMilestone_thenCloseMilestone()
        throws Exception {
        var ghMilestoneMock = mock(GHMilestone.class);
        when(ghMilestoneMock.getState()).thenReturn(GHMilestoneState.OPEN);

        var dueOn = new Date();

        this.githubAction.createGHMilestone("v1.0.0", GHMilestoneState.CLOSED, Optional.of("description"), Optional.of(dueOn), Optional.of(ghMilestoneMock));

        verify(ghActionsKitMock, times(2)).notice(Mockito.anyString());
        verify(ghMilestoneMock).setDueOn(dueOn);
        verify(ghMilestoneMock).setDescription("description");
        verify(ghMilestoneMock, never()).reopen();
        verify(ghMilestoneMock).close();
    }

    /**
     * Test method.
     */
    @Test
    void whenCreateGHMilestoneOpenExistingClosedMilestone_thenOpenMilestone()
        throws Exception {
        var ghMilestoneMock = mock(GHMilestone.class);
        when(ghMilestoneMock.getState()).thenReturn(GHMilestoneState.CLOSED);

        var dueOn = new Date();

        this.githubAction.createGHMilestone("v1.0.0", GHMilestoneState.OPEN, Optional.of("description"), Optional.of(dueOn), Optional.of(ghMilestoneMock));

        verify(ghActionsKitMock, times(2)).notice(Mockito.anyString());
        verify(ghMilestoneMock).setDueOn(dueOn);
        verify(ghMilestoneMock).setDescription("description");
        verify(ghMilestoneMock).reopen();
        verify(ghMilestoneMock, never()).close();
    }

    /**
     * Test method.
     */
    @Test
    void whenCreateGHMilestoneCloseExistingClosedMilestone_thenCloseMilestone()
        throws Exception {
        var ghMilestoneMock = mock(GHMilestone.class);
        when(ghMilestoneMock.getState()).thenReturn(GHMilestoneState.CLOSED);

        var dueOn = new Date();

        this.githubAction.createGHMilestone("v1.0.0", GHMilestoneState.CLOSED, Optional.of("description"), Optional.of(dueOn), Optional.of(ghMilestoneMock));

        verify(ghActionsKitMock).notice(Mockito.anyString());
        verify(ghMilestoneMock).setDueOn(dueOn);
        verify(ghMilestoneMock).setDescription("description");
        verify(ghMilestoneMock, never()).reopen();
        verify(ghMilestoneMock, never()).close();
    }

    /**
     * Test method.
     */
    @Test
    void whenCreateGHMilestoneNull_thenThrowNullPointerException()
        throws Exception {
        var emptyStringOptional = Optional.<String> empty();
        var emptyDateOptional = Optional.<Date> empty();
        var emptyMilestoneOptional = Optional.<GHMilestone> empty();
        assertThrows(NullPointerException.class, () -> this.githubAction.createGHMilestone(null, GHMilestoneState.OPEN, emptyStringOptional, emptyDateOptional, emptyMilestoneOptional));
        assertThrows(NullPointerException.class, () -> this.githubAction.createGHMilestone("v1.0.0", null, emptyStringOptional, emptyDateOptional, emptyMilestoneOptional));
        assertThrows(NullPointerException.class, () -> this.githubAction.createGHMilestone("v1.0.0", GHMilestoneState.OPEN, null, emptyDateOptional, emptyMilestoneOptional));
        assertThrows(NullPointerException.class, () -> this.githubAction.createGHMilestone("v1.0.0", GHMilestoneState.OPEN, emptyStringOptional, null, emptyMilestoneOptional));
        assertThrows(NullPointerException.class, () -> this.githubAction.createGHMilestone("v1.0.0", GHMilestoneState.OPEN, emptyStringOptional, emptyDateOptional, null));
    }

    /**
     * Test method.
     */
    @Test
    void whenDeleteGHMilestonePresent_thenDeleteGhMilestone()
        throws Exception {
        var ghMilestone = Mockito.mock(GHMilestone.class);

        assertDoesNotThrow(() -> {
            this.githubAction.deleteGHMilestone(Optional.of(ghMilestone));
        });

        verify(ghActionsKitMock).notice(Mockito.anyString());
        verify(ghMilestone).delete();
    }

    /**
     * Test method.
     */
    @Test
    void whenDeleteGHMilestoneEmpty_thenLogMessage() {
        assertDoesNotThrow(() -> {
            this.githubAction.deleteGHMilestone(Optional.empty());
        });

        verify(ghActionsKitMock).notice(Mockito.anyString());
    }

    /**
     * Test method.
     */
    @Test
    void whenDeleteGHMilestoneNull_thenThrowNullPointerException()
        throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.deleteGHMilestone(null));
    }
}
