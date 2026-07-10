import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { UiIcon } from '../ui-icon/ui-icon';

/**
 * Optimized YouTube player using the "facade" pattern (à la lite-youtube):
 * only a lightweight thumbnail + play button render up front, and the heavy
 * YouTube iframe is injected on demand when the user hits play. Supports both
 * on-demand videos and real-time live streams (by `videoId` or `channelId`).
 *
 * @example
 * <ui-youtube videoId="jNQXAC9IVRw" title="Committee hearing" />
 * <ui-youtube [live]="true" videoId="jfKfPfyJRdk" title="Council session — LIVE" />
 * <ui-youtube [live]="true" channelId="UCxxxx" title="Plenary — LIVE" />
 */
@Component({
  selector: 'ui-youtube',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiIcon],
  template: `
    <div class="yt">
      @if (loaded()) {
        <iframe
          class="yt__iframe"
          [src]="embedUrl()"
          [title]="title()"
          frameborder="0"
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          referrerpolicy="strict-origin-when-cross-origin"
          allowfullscreen
        ></iframe>
      } @else {
        <button type="button" class="yt__facade" (click)="play()" [attr.aria-label]="'Play: ' + title()">
          @if (thumbnailUrl(); as thumb) {
            <img class="yt__thumb" [src]="thumb" [alt]="title()" loading="lazy" referrerpolicy="no-referrer" />
          }
          <span class="yt__scrim"></span>

          @if (live()) {
            <span class="yt__live"><span class="yt__live-dot"></span> LIVE</span>
          }

          <span class="yt__play">
            <ui-icon name="play_arrow" [size]="34" [fill]="true" />
          </span>

          <span class="yt__meta">
            <ui-icon name="smart_display" [size]="18" [fill]="true" />
            <span class="yt__title">{{ title() }}</span>
          </span>
        </button>
      }
    </div>
  `,
  styles: `
    :host { display: block; }
    .yt {
      position: relative;
      width: 100%;
      aspect-ratio: 16 / 9;
      border-radius: var(--cp-radius);
      overflow: hidden;
      background: #000;
      border: 1px solid var(--cp-outline-variant);
    }
    .yt__iframe { position: absolute; inset: 0; width: 100%; height: 100%; border: 0; }

    .yt__facade {
      position: absolute;
      inset: 0;
      width: 100%;
      height: 100%;
      padding: 0;
      border: none;
      cursor: pointer;
      background: linear-gradient(135deg, var(--cp-primary), var(--cp-primary-container));
      display: block;
    }
    .yt__thumb { position: absolute; inset: 0; width: 100%; height: 100%; object-fit: cover; }
    .yt__scrim {
      position: absolute; inset: 0;
      background: linear-gradient(to top, rgba(0, 0, 0, 0.55), rgba(0, 0, 0, 0.05) 45%, rgba(0, 0, 0, 0.15));
    }

    .yt__play {
      position: absolute;
      top: 50%; left: 50%;
      transform: translate(-50%, -50%);
      width: 64px; height: 64px;
      display: flex; align-items: center; justify-content: center;
      border-radius: var(--cp-radius-full);
      background: rgba(0, 0, 0, 0.55);
      color: #fff;
      transition: transform 0.15s ease, background 0.15s ease;
    }
    .yt__facade:hover .yt__play { background: var(--cp-error); transform: translate(-50%, -50%) scale(1.06); }

    .yt__live {
      position: absolute;
      top: var(--cp-space-sm);
      left: var(--cp-space-sm);
      display: inline-flex;
      align-items: center;
      gap: 6px;
      padding: 4px 10px;
      border-radius: var(--cp-radius);
      background: var(--cp-error);
      color: #fff;
      font-size: 11px;
      font-weight: 700;
      letter-spacing: 0.08em;
    }
    .yt__live-dot {
      width: 7px; height: 7px; border-radius: var(--cp-radius-full); background: #fff;
      animation: yt-pulse 1.4s ease-in-out infinite;
    }
    @keyframes yt-pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.25; } }

    .yt__meta {
      position: absolute;
      left: var(--cp-space-md);
      right: var(--cp-space-md);
      bottom: var(--cp-space-sm);
      display: flex;
      align-items: center;
      gap: var(--cp-space-xs);
      color: #fff;
    }
    .yt__title {
      font-size: 14px; font-weight: 600;
      white-space: nowrap; overflow: hidden; text-overflow: ellipsis;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.6);
    }
  `,
})
export class UiYoutube {
  private readonly sanitizer = inject(DomSanitizer);

  readonly videoId = input<string>();
  readonly channelId = input<string>();
  readonly title = input('Video');
  readonly live = input(false);

  protected readonly loaded = signal(false);

  protected readonly thumbnailUrl = computed(() => {
    const id = this.videoId();
    return id ? `https://i.ytimg.com/vi/${id}/hqdefault.jpg` : null;
  });

  protected readonly embedUrl = computed<SafeResourceUrl>(() => {
    const params = 'autoplay=1&rel=0&modestbranding=1&playsinline=1';
    const channel = this.channelId();
    const base = channel
      ? `https://www.youtube-nocookie.com/embed/live_stream?channel=${channel}`
      : `https://www.youtube-nocookie.com/embed/${this.videoId()}`;
    const sep = base.includes('?') ? '&' : '?';
    return this.sanitizer.bypassSecurityTrustResourceUrl(`${base}${sep}${params}`);
  });

  protected play(): void {
    this.loaded.set(true);
  }
}
