import { ChangeDetectionStrategy, Component } from '@angular/core';
import { UiYoutube } from '../../../../shared/ui/ui-youtube/ui-youtube';
import { UiIcon } from '../../../../shared/ui/ui-icon/ui-icon';
import { TranslatePipe } from '../../../../shared/pipes/translate.pipe';

// TODO: replace with the real political broadcast that is live right now.
// A YouTube live video id, or use [channelId] on <ui-youtube> for a channel's live_stream.
const LIVE_VIDEO_ID = 'jfKfPfyJRdk';

/** Sidebar widget featuring a real-time live broadcast. */
@Component({
  selector: 'app-live-now',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [UiYoutube, UiIcon, TranslatePipe],
  template: `
    <div class="live">
      <header class="live__head">
        <span class="live__badge"><span class="live__dot"></span> {{ 'label.live' | translate: 'LIVE' }}</span>
        <span class="live__viewers">
          <ui-icon name="visibility" [size]="16" />
          3.2k {{ 'label.watching' | translate: 'watching' }}
        </span>
      </header>
      <ui-youtube [live]="true" [videoId]="liveVideoId" [title]="'label.plenary-live' | translate: 'Plenary session — live now'" />
      <div class="live__caption">{{ 'label.plenary-caption' | translate: 'Plenary session · State Legislature' }}</div>
    </div>
  `,
  styles: `
    :host { display: block; }
    .live {
      background: var(--cp-surface-container-lowest);
      border: 1px solid var(--cp-outline-variant);
      border-radius: var(--cp-radius-lg);
      box-shadow: var(--cp-shadow-card);
      padding: var(--cp-space-md);
    }
    .live__head {
      display: flex; justify-content: space-between; align-items: center;
      margin-bottom: var(--cp-space-sm);
    }
    .live__badge {
      display: inline-flex; align-items: center; gap: 6px;
      padding: 4px 10px; border-radius: var(--cp-radius);
      background: var(--cp-error); color: #fff;
      font-size: 11px; font-weight: 700; letter-spacing: 0.08em;
    }
    .live__dot {
      width: 7px; height: 7px; border-radius: var(--cp-radius-full); background: #fff;
      animation: live-pulse 1.4s ease-in-out infinite;
    }
    @keyframes live-pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.25; } }
    .live__viewers {
      display: inline-flex; align-items: center; gap: var(--cp-space-xs);
      font-size: 13px; color: var(--cp-on-surface-variant); font-weight: 600;
    }
    .live__caption { margin-top: var(--cp-space-sm); font-size: 13px; color: var(--cp-on-surface-variant); }
  `,
})
export class LiveNow {
  protected readonly liveVideoId = LIVE_VIDEO_ID;
}
