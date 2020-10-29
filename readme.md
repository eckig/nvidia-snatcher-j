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
| `LOAD_INTERVAL` | Interval | Wait time between requests (in seconds). Default: `20` |
| `LOAD_MAX_WAIT` | Max. wait time | The max. time (in seconds) to wait for a page to load. Default: 3x `LOAD_INTERVAL` |
| `LOAD_PARALLELISM` | Parallelism | The number of max. parallel request to send. Default: `2` |
| `GMAIL_USER` | GMail password | If empty, no GMail notifications |
| `GMAIL_PASSWORD` | GMail address | If empty, no GMail notifications |
| `SCRAPER_STORES` | Supported stores you want to be scraped | comma separated list of stores, e.g. `nvidia_de_de, nvidia_en_us, nbb` |
| `SCRAPER_MODELS` | Supported models you want to be scraped | comma separated list of model, e.g. `3080_fe, 3090_fe, 3070_fe` |
| `NOTIFY_ON_CHANGE` | Notify on status changed | Default is `off`, only "in stock" status will trigger a notification. Set to `on` to get notified when the status changed. |

> If you have multi-factor authentication (MFA), you will need to create an [app password](https://myaccount.google.com/apppasswords) and use this instead of your Gmail password.
