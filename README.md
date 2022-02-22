[![Build](https://github.com/julbme/gh-action-manage-milestone/actions/workflows/maven-build.yml/badge.svg)](https://github.com/julbme/gh-action-manage-milestone/actions/workflows/maven-build.yml)
[![Lint Commit Messages](https://github.com/julbme/gh-action-manage-milestone/actions/workflows/commitlint.yml/badge.svg)](https://github.com/julbme/gh-action-manage-milestone/actions/workflows/commitlint.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=julbme_gh-action-manage-milestone&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=julbme_gh-action-manage-milestone)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/julbme/gh-action-manage-milestone)

# GitHub Action to manage Milestones

The GitHub Action for managing milestones of the GitHub repository.

- Create a new milestone
- Edit an existing milestone
- Closing an existing milestone.
- Deleting a milestone.

## Usage

### Example Workflow file

- Create a milestone:

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Create the milestone
        uses: julbme/gh-action-manage-milestone@v1
        with:
          title: Some title
          state: open
          description: Some description
          due_on: "2022-01-01"
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

- Close the milestone

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Close the milestone
        uses: julbme/gh-action-manage-milestone@v1
        with:
          title: Some title
          state: closed
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

- Delete the milestone

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Close the milestone
        uses: julbme/gh-action-manage-milestone@v1
        with:
          title: Some title
          state: deleted
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### Inputs

| Name          | Type   | Default   | Description                                                           |
| ------------- | ------ | --------- | --------------------------------------------------------------------- |
| `title`       | string | `Not set` | Title of the milestone. **Required**                                  |
| `state`       | string | `open`    | State of the milestone. Valid values are `open`, `closed`, `deleted`  |
| `description` | string | `Not set` | Description of the milestone of the milestone.                        |
| `due_on`      | string | `Not set` | ISO8601 representation of the due date of the milestone. `yyyy-MM-dd` |

### Outputs

| Name     | Type   | Description                                                   |
| -------- | ------ | ------------------------------------------------------------- |
| `number` | number | ID of the milestone, or ` ` in case the milestone is deleted. |

## Contributing

This project is totally open source and contributors are welcome.