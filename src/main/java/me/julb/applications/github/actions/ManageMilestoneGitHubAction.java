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

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletionException;

import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHMilestone;
import org.kohsuke.github.GHMilestoneState;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import me.julb.sdk.github.actions.kit.GitHubActionsKit;
import me.julb.sdk.github.actions.spi.GitHubActionProvider;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;

/**
 * The action to manage milestones. <br>
 * @author Julb.
 */
public class ManageMilestoneGitHubAction implements GitHubActionProvider {

    /**
     * The GitHub action kit.
     */
    @Setter(AccessLevel.PACKAGE)
    private GitHubActionsKit ghActionsKit = GitHubActionsKit.INSTANCE;

    /**
     * The GitHub API.
     */
    @Setter(AccessLevel.PACKAGE)
    private GitHub ghApi;

    /**
     * The GitHub repository.
     */
    @Setter(AccessLevel.PACKAGE)
    private GHRepository ghRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        try {
            // Get inputs
            var milestoneTitle = getInputTitle();
            var milestoneState = getInputState();
            var milestoneDescription = getInputDescription();
            var milestoneDueOnDate = getInputDueOn();

            // Trace parameters
            ghActionsKit.debug(String.format(
                    "parameters: [title: %s, state: %s, description: %s, due_on: %s]",
                    milestoneTitle,
                    milestoneState.name(),
                    milestoneDescription.orElse(""),
                    milestoneDueOnDate.map(Date::toString).orElse("")));

            // Read GitHub repository.
            connectApi();

            // Retrieve repository
            ghRepository = ghApi.getRepository(ghActionsKit.getGitHubRepository());

            // Get milestone
            var existingGHMilestone = getGHMilestone(milestoneTitle);

            // Creation path.
            if (InputMilestoneState.OPEN.equals(milestoneState) || InputMilestoneState.CLOSED.equals(milestoneState)) {
                // Convert input state to GH State
                var ghMilestoneState = GHMilestoneState.valueOf(milestoneState.name());

                // Create milestone.
                var ghMilestone = createGHMilestone(
                        milestoneTitle,
                        ghMilestoneState,
                        milestoneDescription,
                        milestoneDueOnDate,
                        existingGHMilestone);

                // Set output.
                ghActionsKit.setOutput(OutputVars.NUMBER.key(), ghMilestone.getNumber());
            } else {
                // Delete milestone if exist
                deleteGHMilestone(existingGHMilestone);

                // Set empty output.
                ghActionsKit.setEmptyOutput(OutputVars.NUMBER.key());
            }
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    // ------------------------------------------ Utility methods.

    /**
     * Gets the "title" input.
     * @return the "title" input.
     */
    String getInputTitle() {
        return ghActionsKit.getRequiredInput("title");
    }

    /**
     * Gets the "state" input.
     * @return the "state" input.
     */
    InputMilestoneState getInputState() {
        return ghActionsKit.getEnumInput("state", InputMilestoneState.class).orElse(InputMilestoneState.OPEN);
    }

    /**
     * Gets the "description" input.
     * @return the "description" input.
     */
    Optional<String> getInputDescription() {
        return ghActionsKit.getInput("description");
    }

    /**
     * Gets the "due_on" input.
     * @return the "due_on" input.
     */
    Optional<Date> getInputDueOn() {
        return ghActionsKit.getInput("due_on").map(dateStr -> {
            try {
                // GitHub requires the date to be set to 8:00am
                var instant = Instant.parse(String.format("%sT08:00:00.000Z", dateStr));
                return Date.from(instant);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(e);
            }
        });
    }

    /**
     * Connects to GitHub API.
     * @throws IOException if an error occurs.
     */
    void connectApi() throws IOException {
        ghActionsKit.debug("github api url connection: check.");

        // Get token
        var githubToken = ghActionsKit.getRequiredEnv("GITHUB_TOKEN");

        // @formatter:off
        ghApi = Optional.ofNullable(ghApi)
                .orElse(new GitHubBuilder()
                        .withEndpoint(ghActionsKit.getGitHubApiUrl())
                        .withOAuthToken(githubToken)
                        .build());
        ghApi.checkApiUrlValidity();
        ghActionsKit.debug("github api url connection: ok.");
        // @formatter:on
    }

    /**
     * Gets the {@link GHMilestone} milestone matching the given title.
     * @param title the milestone title to look for.
     * @return the {@link GHMilestone} for the given title if exists, <code>false</code> otherwise.
     * @throws IOException if an error occurs.
     */
    Optional<GHMilestone> getGHMilestone(@NonNull String title) throws IOException {

        for (GHMilestone ghMilestone : ghRepository.listMilestones(GHIssueState.ALL)) {
            if (ghMilestone.getTitle().equalsIgnoreCase(title)) {
                return Optional.of(ghMilestone);
            }
        }

        return Optional.empty();
    }

    /**
     * Creates or updates the {@link GHMilestone} if any.
     * @param title the milestone title.
     * @param state the milestone state.
     * @param description the milestone description, or {@link Optional#empty()} if the description is empty.
     * @param dueOn the milestone due on date, or {@link Optional#empty()} if there is no due date.
     * @param existingMilestone the existing milestone, or {@link Optional#empty()} if there is no existing milestone.
     * @return the {@link GHMilestone} created or updated.
     * @throws IOException if an error occurs.
     */
    GHMilestone createGHMilestone(
            @NonNull String title,
            @NonNull GHMilestoneState state,
            @NonNull Optional<String> description,
            @NonNull Optional<Date> dueOn,
            @NonNull Optional<GHMilestone> existingMilestone)
            throws IOException {
        GHMilestone ghMilestoneManaged;

        if (existingMilestone.isEmpty()) {
            // The milestone does not exist: create
            ghActionsKit.notice("creating the milestone.");
            ghMilestoneManaged = ghRepository.createMilestone(title, description.orElse(null));
        } else {
            // The milestone already exists: update description
            ghActionsKit.notice("updating the milestone");
            ghMilestoneManaged = existingMilestone.get();
        }

        // update description
        if (description.isPresent()) {
            ghMilestoneManaged.setDescription(description.get());
        }

        // update due on
        if (dueOn.isPresent()) {
            ghMilestoneManaged.setDueOn(dueOn.get());
        }

        // update state
        if (!state.equals(ghMilestoneManaged.getState())) {
            if (GHMilestoneState.OPEN.equals(state)) {
                ghActionsKit.notice("updating the state => OPEN");
                ghMilestoneManaged.reopen();
            } else {
                ghActionsKit.notice("updating the state => CLOSED");
                ghMilestoneManaged.close();
            }
        }

        return ghMilestoneManaged;
    }

    /**
     * Deletes the {@link GHMilestone} if any.
     * @param milestoneToDelete the {@link GHMilestone} to delete, or {@link Optional#empty()}.
     * @throws IOException if an error occurs.
     */
    void deleteGHMilestone(@NonNull Optional<GHMilestone> milestoneToDelete) throws IOException {
        // Check if milestone exists.
        if (milestoneToDelete.isPresent()) {
            // The milestone exists: delete.
            ghActionsKit.notice("deleting the milestone.");
            milestoneToDelete.get().delete();
        } else {
            // The milestone does not exist, nothing to do.
            ghActionsKit.notice("skipping milestone deletion as it does not exist.");
        }
    }
}
