import {
  afterNextRender,
  Directive,
  ElementRef,
  inject,
  input,
  OnDestroy,
  output,
} from '@angular/core';

/**
 * Emits `loadMore` when the host element (a sentinel placed at the end of a
 * list) scrolls into view. Drives infinite scrolling for the directory pages.
 *
 * @example
 * <div appInfiniteScroll [disabled]="allLoaded()" (loadMore)="more()"></div>
 */
@Directive({
  selector: '[appInfiniteScroll]',
})
export class InfiniteScrollDirective implements OnDestroy {
  private readonly host = inject(ElementRef<HTMLElement>);

  /** When true, the sentinel stops requesting more (nothing left to load). */
  readonly disabled = input(false);
  readonly loadMore = output<void>();

  private observer?: IntersectionObserver;

  constructor() {
    afterNextRender(() => {
      this.observer = new IntersectionObserver(
        (entries) => {
          if (entries[0]?.isIntersecting && !this.disabled()) {
            this.loadMore.emit();
          }
        },
        { rootMargin: '300px' },
      );
      this.observer.observe(this.host.nativeElement);
    });
  }

  ngOnDestroy(): void {
    this.observer?.disconnect();
  }
}
