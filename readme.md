I created this small project out of curiosity on how to build something like the [nvidia-snatcher by jef](https://github.com/jef/nvidia-snatcher) in java.

#### Docker image

Available via GitHub Container Registry.

| Tag | Note |
|:---:|---|
| `latest` | Latest nightly build |

Use `docker pull ghcr.io/eckig/nvidia-snatcher-j:latest` to get image.

#### Customization

| Environment variable | Description | Notes |
|:---:|---|---|
| `SCRAPER_INTERVAL` | Interval | Wait time between requests (in seconds), default `20` |
| `GMAIL_USER` | Gmail password | If empty, no GMail notifications |
| `GMAIL_PASSWORD` | Gmail address | If empty, no GMail notifications |

> If you have multi-factor authentication (MFA), you will need to create an [app password](https://myaccount.google.com/apppasswords) and use this instead of your Gmail password.
