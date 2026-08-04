import { ChangeDetectionStrategy, Component, ElementRef, afterNextRender, effect, input, signal, viewChild } from '@angular/core';

/**
 * Clamps long text to a fixed number of lines with a trailing ellipsis, revealing the rest behind
 * a "show more"/"show less" toggle — the same pattern used for post bodies and comments on most
 * social networks, so a handful of long entries don't push everything else off screen.
 *
 * Whether the toggle appears at all is decided by measuring the rendered element (scrollHeight vs
 * clientHeight) rather than a character count, since the same text clamps differently depending on
 * container width and font size.
 *
 * @example <ui-expandable-text [text]="post().content" [maxLines]="6" />
 */
@Component({
  selector: 'ui-expandable-text',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <p #textEl class="expandable-text" [class.expandable-text--clamped]="!expanded()" [style.--ui-clamp-lines]="maxLines()">{{ text() }}</p>
    @if (overflowing()) {
      <button type="button" class="expandable-text__toggle" (click)="expanded.set(!expanded())">
        {{ expanded() ? lessLabel() : moreLabel() }}
      </button>
    }
  `,
  styles: `
    :host { display: block; }
    .expandable-text {
      margin: 0;
      white-space: pre-wrap;
      overflow-wrap: anywhere;
    }
    .expandable-text--clamped {
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: var(--ui-clamp-lines);
      overflow: hidden;
    }
    .expandable-text__toggle {
      margin-top: 4px;
      border: none;
      background: transparent;
      padding: 0;
      color: var(--cp-secondary);
      font-size: 13px;
      font-weight: 700;
      cursor: pointer;
    }
    .expandable-text__toggle:hover { text-decoration: underline; }
  `,
})
export class UiExpandableText {
  readonly text = input.required<string>();
  readonly maxLines = input(4);
  readonly moreLabel = input('Show more');
  readonly lessLabel = input('Show less');

  protected readonly expanded = signal(false);
  protected readonly overflowing = signal(false);

  private readonly textEl = viewChild.required<ElementRef<HTMLElement>>('textEl');

  constructor() {
    afterNextRender(() => this.measure());
    effect(() => {
      this.text();
      this.maxLines();
      queueMicrotask(() => this.measure());
    });
  }

  private measure(): void {
    if (this.expanded()) {
      return;
    }
    const el = this.textEl().nativeElement;
    this.overflowing.set(el.scrollHeight > el.clientHeight + 1);
  }
}
