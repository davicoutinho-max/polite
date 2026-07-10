import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SideNav } from '../side-nav/side-nav';
import { AppTopBar } from '../app-top-bar/app-top-bar';
import { MobileBottomNav } from '../mobile-bottom-nav/mobile-bottom-nav';

/** Application shell: fixed side navigation + an independently scrolling content area. */
@Component({
  selector: 'app-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, SideNav, AppTopBar, MobileBottomNav],
  template: `
    <div class="shell">
      <app-side-nav />
      <div class="shell__body">
        <app-top-bar />
        <main class="shell__content">
          <div class="shell__inner">
            <router-outlet />
          </div>
        </main>
        <app-mobile-bottom-nav />
      </div>
    </div>
  `,
  styles: `
    :host { display: block; }
    .shell {
      display: flex;
      height: 100dvh;
      overflow: hidden;
    }
    .shell__body {
      flex: 1;
      min-width: 0;
      height: 100dvh;
      display: flex;
      flex-direction: column;
    }
    .shell__content {
      flex: 1;
      min-width: 0;
      overflow-y: auto;
      overflow-x: hidden;
      overscroll-behavior: contain;
    }
    /* Centering rail so ultra-wide screens stay readable while filling space.
       min-height:100% lets full-height pages (e.g. Messages) fill the viewport
       while normal pages still grow and scroll naturally. */
    .shell__inner {
      width: 100%;
      max-width: 1600px;
      min-height: 100%;
      margin: 0 auto;
    }
    @media (max-width: 767px) {
      .shell__content { padding-bottom: 64px; }
    }
  `,
})
export class Shell {}
