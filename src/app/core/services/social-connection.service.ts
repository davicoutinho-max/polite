import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

export type SocialPlatform = 'facebook' | 'instagram' | 'x';

export interface SocialConnection {
  readonly platform: SocialPlatform;
  readonly externalAccountId: string;
  readonly externalAccountName: string | null;
  readonly connectedAt: string;
}

export interface SocialShare {
  readonly platform: SocialPlatform;
  readonly status: 'published' | 'failed';
  readonly externalPostId: string | null;
  readonly errorMessage: string | null;
  readonly sharedAt: string;
}

interface StartConnectResponseDto {
  readonly url: string;
}

/** Real Meta (Facebook+Instagram) and X OAuth connect flow, plus real cross-posting of a
 * published feed post to whichever of those networks the citizen has connected — see
 * feed-content-service's SocialConnectionController/SocialShareController. Connecting redirects
 * the whole browser to the provider's own consent screen (not an XHR), since that's how OAuth
 * works — the caller of startMetaConnect/startXConnect is expected to do
 * `window.location.href = url`, not treat this like a normal API call. */
@Injectable({ providedIn: 'root' })
export class SocialConnectionService {
  private readonly http = inject(HttpClient);
  private readonly apiBase = `${environment.apiBaseUrl}/api/feed/social-connections`;

  listConnections(): Observable<SocialConnection[]> {
    return this.http.get<SocialConnection[]>(this.apiBase);
  }

  startMetaConnect(): Observable<string> {
    return this.http.post<StartConnectResponseDto>(`${this.apiBase}/meta/start`, {}).pipe(map((res) => res.url));
  }

  startXConnect(): Observable<string> {
    return this.http.post<StartConnectResponseDto>(`${this.apiBase}/x/start`, {}).pipe(map((res) => res.url));
  }

  disconnect(platform: SocialPlatform): Observable<void> {
    return this.http.delete<void>(`${this.apiBase}/${platform}`);
  }

  publish(postId: string, platforms: SocialPlatform[]): Observable<SocialShare[]> {
    return this.http.post<SocialShare[]>(`${environment.apiBaseUrl}/api/feed/posts/${postId}/social-shares`, { platforms });
  }
}
