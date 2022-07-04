# Contributing

If you want to add a feature or make a change to the code you may create a branch in our repo.

## Pull Request Process

* If this is an R&D internal change or bug then we will want to create a ticket on the
  [BEFOUND](https://backbase.atlassian.net/browse/BEFOUND) backlog for you, please contact us in Slack [#backend-foundation-team](https://backbase.slack.com/archives/C01EQ60LXK8).
* If this is a new features for a customer please use the [RFF](https://backbase.atlassian.net/browse/RFF) project.
  [New feature request writing guide](https://backbase.atlassian.net/wiki/spaces/ES/pages/3520495625/New+feature+request+writing+guide).
* If this is a customer bug please see [How to report in MAINT](https://backbase.atlassian.net/wiki/spaces/PROD/pages/1320583655/How+to+report+in+MAINT).

## Our Standards

Many of our products are widely used in other applications as such we place an emphasis on changes following SEMVER and
not introducing breaking changes. There should always be comprehensive unit tests and end to end tests. We also need to fully document any new features or provide migration guides.

## Our Responsibilities

* Backlog: https://backbase.atlassian.net/browse/BEFOUND search for issues with the
  [ideal-for-contribution](https://backbase.atlassian.net/issues/?jql=project%20%3D%20BEFOUND%20AND%20status%20in%20(%22In%20Analysis%22%2C%20Open%2C%20%22Ready%20for%20Dev%22)%20AND%20labels%20%3D%20ideal-for-contribution) tag.


### Bug reports
For bug reports a rule of thumb is to provide as much information as you can including replication steps, expected result, logs etc. Lack of information will slow down the process. In most cases, Service SDK team will verify the bug fix, however on rare occasions the issue reporter could be asked to verify on their capability with Service SDK cr version containing the fix.

```
h3. Background 
What are you trying to do?
How serious is the issue?
Does this block anything?

h3. Steps to reproduce the issue
Please include steps to replicate the issue, including any software versions.
Steps must be clear and detailed enough for someone outside of your team to understand.

h3. Expected behaviour
What behaviour is expected?

h3. Actual behaviour
What actually happens?

h3. Analysis/Development Notes
Include logs at the time of the issue.
Include link to a branch where the issue was encountered.
Are there any workarounds or fixes?
Are there any risks in the fixes.
```

### New Feature / Change Request / Improvement with a PR
For new features it is important we understand the motivation for requirements, don’t just create a PR or request without enough information for others to understand a feature. Same applies for the change request or an improvement.
Documentation for the plugin is [here](https://github.com/Backbase/intellij-docs) (published [here](http://engineering.backbase.com/intellij-docs/)) and will need to be updated if there are any changes or new features, either create a documentation PR or send the text to us to integrate in the documentation.

#### Story template

```
h2. Value
What value will this feature or change add? 

h2. Acceptance Criteria
The requirements to be met in order for the story to be Done. 
AC must be high-level and not specific to the solution, testable and clear for anyone reading the story.

# AC 1
# AC 2

h2. Notes
*Design and implementation*
*Risks*
*Documentation updates*
*Migration guide*

h2. Testing Notes
Recommendation on how the testing of this feature could be approached?
```