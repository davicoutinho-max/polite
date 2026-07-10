import { computed, inject, Injectable, signal } from '@angular/core';
import { FeedSort, Post, PostDraft, StatusTag } from '../models';
import { SessionService } from './session.service';

const COMMENT_AVATAR =
  'https://lh3.googleusercontent.com/aida-public/AB6AXuCE0D7JPXs4LxauFS-kbprWYD0-f7RD4Ydp-sfmuPS7GeKrwOzmWLMcM8So2XYtuMo0XRoKB7SSJjtsNMISN-k8Ir3lE5sh4D9A0hBEaXTEfegcl9xBAvm-Y1HJ9KR2mu2-pRJFtTe_dLVXrLZL89YvJipXpEpEMc0Yaz6ZnDIWEpRJ8_Z4xKTl6HEZocsNuZlqHHzZi2Lnvz37jInV5Ae79N_XeulYJMqQw8VN7FXRSeKD4Uvd5UoSaA';

// TODO: replace with the actual political demonstration clip.
const DEMO_VIDEO_ID = 'jNQXAC9IVRw';

const INITIAL_POSTS: Post[] = [
  {
    id: 'pv',
    author: {
      id: 'jane-doe',
      name: 'Senator Jane Doe',
      verified: true,
      role: 'State Legislature',
      avatarUrl: COMMENT_AVATAR,
    },
    createdAt: '30 minutes ago',
    context: 'Committee hearing',
    content:
      'Watch my full statement at today’s hearing on the Open Municipal Budget Ledger. We walked the committee through how citizens will track every public expense in real time.',
    tags: [
      { label: '#Transparency', severity: 'secondary' },
      { label: 'Hearing', severity: 'info' },
    ],
    videoId: DEMO_VIDEO_ID,
    visibility: 'public',
    metrics: { likes: 512, comments: 0, liked: false },
    comments: [],
  },
  {
    id: 'p1',
    author: {
      id: 'jane-doe',
      name: 'Senator Jane Doe',
      verified: true,
      role: 'State Legislature',
      avatarUrl:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuDNF06tFrfgVfuEw_FOHQKoJQ-FGxIeD-WKKUf4bFAfJRNfHinpNj7IPKjXwJZ-BwhQhF5TBOJdOcahM6PA4rSbKCvV0Y9GUSm748-U1fFOS7Tv6AEJ-U6nJK75Cp_U9uPx1ebSN9gYtaN1t7AC4T7l2iXovmj25qvTxScJgZ0D0MVacpDHIs87kOvXrgibiMZj9zmtR_Oyed2kt01LUJlA5h_EHb7Yp1Ie1MVH0QLC5Bs06fXy4OuKpw',
    },
    createdAt: '2 hours ago',
    context: 'State Legislature',
    content:
      "I've just introduced Bill H.R. 402, aimed at increasing transparency in local municipal budgets. It's crucial that citizens know exactly where their tax dollars are going. We are proposing a centralized, accessible digital ledger for all expenditures over $10,000.",
    tags: [
      { label: '#Policy', severity: 'secondary' },
      { label: 'In Committee', severity: 'success' },
    ],
    imageUrl:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDcwtZMp_Ub8Iaxnxn0nIBKjuyiO59s9eeL1KjTsmUplu0TGgEflGL4OzArW6eHJZQQeg_3LMMldQBc51-dCPruSrmLnwwWgOaGzaaWLCNtgANQmmqlAMNsz2nFCweg0tri5BUHz5clRLu5SipDVUCcHc_lMjqM3PSeYag0aNYamVAAiLw3m63KSYHW7BEpa4taarkg-jiviL687gdze5w0wyaIUZRKxvRvABhxRklsm80zHeR4vsQTKQ',
    visibility: 'public',
    metrics: { likes: 1200, comments: 2, liked: false },
    comments: [
      {
        id: 'c1',
        author: { id: 'marcus-chen', name: 'Rep. Marcus Chen', verified: true, avatarUrl: COMMENT_AVATAR },
        text: 'Fully support this. Transparency is the foundation of trust.',
        timeLabel: '1h',
      },
      {
        id: 'c2',
        author: { id: 'citizen-42', name: 'Ana Ribeiro', avatarUrl: COMMENT_AVATAR },
        text: 'Finally! When does the ledger go live?',
        timeLabel: '32m',
      },
    ],
  },
  {
    id: 'p2',
    author: {
      id: 'marcus-chen',
      name: 'Rep. Marcus Chen',
      verified: true,
      role: 'City Council',
      avatarUrl:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuCE0D7JPXs4LxauFS-kbprWYD0-f7RD4Ydp-sfmuPS7GeKrwOzmWLMcM8So2XYtuMo0XRoKB7SSJjtsNMISN-k8Ir3lE5sh4D9A0hBEaXTEfegcl9xBAvm-Y1HJ9KR2mu2-pRJFtTe_dLVXrLZL89YvJipXpEpEMc0Yaz6ZnDIWEpRJ8_Z4xKTl6HEZocsNuZlqHHzZi2Lnvz37jInV5Ae79N_XeulYJMqQw8VN7FXRSeKD4Uvd5UoSaA',
    },
    createdAt: '5 hours ago',
    context: 'City Council',
    content:
      'Public transit expansion is officially funded for the next fiscal year. Three new lines, extended weekend service, and accessible stations across every district. Transparency report and full budget breakdown will be published on the open ledger this Friday.',
    tags: [
      { label: '#Transit', severity: 'secondary' },
      { label: 'Passed', severity: 'success' },
    ],
    visibility: 'public',
    metrics: { likes: 864, comments: 1, liked: true },
    comments: [
      {
        id: 'c1',
        author: { id: 'green-coalition', name: 'Green Coalition', avatarUrl: COMMENT_AVATAR },
        text: 'Great news for accessibility across the city.',
        timeLabel: '20m',
      },
    ],
  },
  {
    id: 'p3',
    author: {
      id: 'green-coalition',
      name: 'Green Coalition',
      verified: false,
      role: 'Community',
      avatarUrl:
        'https://lh3.googleusercontent.com/aida-public/AB6AXuBfLxYlq8drGMj6_MPWgFtzX7vBUdily37sBqP2qqDgu6Pr4snpjPwswLUuRi551U0HSSeo-ATVCth_kekH52TSm63uIHxtXhYT7DKWwah7JKJdtNm87kTmzW-PkNjenweQVV7ArrkSGdD65jKJziFdFB8A0egariUvsXLjqr56Bv0nJnp2fxm29q89UChlOGvGWHP3_RXCyqhOKrgwXMNtBgKLzVVAMZBz0JEIdys087x9l4pwXTYjxQ',
    },
    createdAt: '1 day ago',
    context: 'Environment',
    content:
      'The Clean Energy Subsidy proposal reached 8.9K signatures today. Thank you to every citizen making their voice heard. Next step: a public hearing scheduled for the end of the month.',
    tags: [
      { label: '#Environment', severity: 'success' },
      { label: 'Petition', severity: 'info' },
    ],
    visibility: 'public',
    metrics: { likes: 431, comments: 0, liked: false },
    comments: [],
  },
  {
    id: 'p4',
    author: {
      id: 'progressive',
      name: 'Progressive Party',
      verified: true,
      role: 'Official party account',
      avatarUrl: COMMENT_AVATAR,
    },
    createdAt: '2 days ago',
    context: 'City Hall',
    kind: 'agenda',
    content: 'Join us for our National Convention — open to every affiliated member.',
    tags: [{ label: '#Agenda', severity: 'info', icon: 'event' }],
    agenda: { title: 'National Convention 2026', date: 'Aug 12, 2026 · 09:00', location: 'Brasília' },
    visibility: 'public',
    metrics: { likes: 289, comments: 0, liked: false },
    comments: [],
  },
];

/**
 * Reactive feed store. Exposes signals only; mutations go through intent
 * methods so components stay declarative.
 */
@Injectable({ providedIn: 'root' })
export class FeedService {
  private readonly session = inject(SessionService);

  private readonly _posts = signal<Post[]>(INITIAL_POSTS);
  private readonly _sort = signal<FeedSort>('top');

  readonly sort = this._sort.asReadonly();

  /** Posts ordered according to the active sort. */
  readonly posts = computed<Post[]>(() => {
    const posts = [...this._posts()];
    switch (this._sort()) {
      case 'top':
        return posts.sort((a, b) => b.metrics.likes - a.metrics.likes);
      case 'latest':
        return posts; // insertion order = newest first
      case 'following':
        return posts.filter((p) => p.author.verified);
    }
  });

  setSort(sort: FeedSort): void {
    this._sort.set(sort);
  }

  /** Posts authored by a given user/party id, e.g. for a profile's Activity tab. */
  postsByAuthor(authorId: string) {
    return computed(() => this._posts().filter((p) => p.author.id === authorId));
  }

  toggleLike(postId: string): void {
    this._posts.update((posts) =>
      posts.map((post) =>
        post.id === postId
          ? {
              ...post,
              metrics: {
                ...post.metrics,
                liked: !post.metrics.liked,
                likes: post.metrics.likes + (post.metrics.liked ? -1 : 1),
              },
            }
          : post,
      ),
    );
  }

  publish(draft: PostDraft): void {
    const { kind, visibility } = draft;
    const text = draft.text.trim();
    const author = this.session.currentUser();
    const base = {
      id: `p${Date.now()}`,
      author,
      createdAt: 'just now',
      visibility,
      metrics: { likes: 0, comments: 0, liked: false },
      comments: [] as Post['comments'],
    };

    let newPost: Post;
    if (kind === 'agenda' && draft.agenda) {
      const tags: StatusTag[] = [{ label: '#Agenda', severity: 'info', icon: 'event' }];
      if (visibility === 'private') {
        tags.push({ label: 'Private', severity: 'neutral', icon: 'lock' });
      }
      newPost = { ...base, context: draft.agenda.location, content: text, tags, agenda: draft.agenda, kind };
    } else if (kind === 'live' && draft.live) {
      const tags: StatusTag[] = [{ label: '#Live', severity: 'danger', icon: 'sensors' }];
      if (visibility === 'private') {
        tags.push({ label: 'Private', severity: 'neutral', icon: 'lock' });
      }
      newPost = {
        ...base,
        context: draft.live.isLiveNow ? 'Live now' : 'Scheduled live',
        content: text,
        tags,
        live: draft.live,
        kind,
      };
    } else {
      if (!text) {
        return;
      }
      const tags: StatusTag[] = [{ label: '#Discussion', severity: 'secondary' }];
      if (visibility === 'private') {
        tags.push({ label: 'Private', severity: 'neutral', icon: 'lock' });
      }
      newPost = { ...base, context: visibility === 'private' ? 'Private' : 'Your feed', content: text, tags };
    }

    this._posts.update((posts) => [newPost, ...posts]);
  }

  addComment(postId: string, text: string): void {
    const body = text.trim();
    if (!body) {
      return;
    }
    const author = this.session.currentUser();
    this._posts.update((posts) =>
      posts.map((post) =>
        post.id === postId
          ? {
              ...post,
              metrics: { ...post.metrics, comments: post.metrics.comments + 1 },
              comments: [
                ...post.comments,
                { id: `c${Date.now()}`, author, text: body, timeLabel: 'now' },
              ],
            }
          : post,
      ),
    );
  }
}
