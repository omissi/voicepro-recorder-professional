# Termux commands

```bash
cd ~/storage/downloads
unzip -o voicepro-recorder-professional-source.zip
cd voicepro-recorder-professional

git init
git branch -M main
git remote remove origin 2>/dev/null || true
git remote add origin https://github.com/omissi/voicepro-recorder-professional.git

git status
git add .
git commit -m "Create VoicePro Recorder professional Android app"
gh repo create omissi/voicepro-recorder-professional --public --source=. --remote=origin --push

gh workflow run android-build.yml --repo omissi/voicepro-recorder-professional --ref main
gh run list --repo omissi/voicepro-recorder-professional --limit 5
gh run watch --repo omissi/voicepro-recorder-professional
```
