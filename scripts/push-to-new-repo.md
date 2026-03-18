# Push this codebase to a new repo (no fork, no secrets in history)

Use this when you create a **new empty repo** on GitHub and want to push your current code as a single clean commit with **no secrets in history** (so GitHub Push Protection won't block you).

## 1. Create the new repo on GitHub

- Create a new **empty** repository (no README, no .gitignore).
- Note the repo URL, e.g. `https://github.com/farhanfaiz-wl/concerto-eventservice.git`

## 2. In this project folder, create a fresh branch with no history

From the repo root (`concerto-eventservice`):

```bash
# Create a branch with no parent (no history)
git checkout --orphan new-main

# Stage everything except files in .gitignore (secrets stay untracked)
git add -A

# One initial commit
git commit -m "Initial commit: EventService (no secrets in history)"
```

## 3. Point at your new repo and push

If your new repo is already the remote `origin`:

```bash
git push -u origin new-main
```

To push this branch as `main` on the new repo:

```bash
git push -u origin new-main:main
```

To use a different remote name (e.g. `newrepo`):

```bash
git remote add newrepo https://github.com/YOUR_ORG/your-new-repo.git
git push -u newrepo new-main:main
```

## 4. Local testing with real credentials

- **Twilio / NLU:** Set environment variables (`TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, etc.) or add a local config file (e.g. `application-local.yml`) that is in `.gitignore`.
- **Google JSON:** Keep `google_business.json` and `google_creds.json` only on your machine; they are in `.gitignore` and will not be committed.

## 5. Optional: switch back to your old branch

To continue working on the original `develop` history locally:

```bash
git checkout develop
```

To make `new-main` your default branch and delete the orphan branch name:

```bash
git branch -m new-main main
```
