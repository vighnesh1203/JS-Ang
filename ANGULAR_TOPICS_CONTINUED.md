# Angular Deep Dive - Topics 2-14

🎯 **ANSWER KEY REFERENCE**: For detailed expected answers to all Angular interview questions in this guide, see [INTERVIEW_ANSWERS_COMPREHENSIVE_KEY.md](INTERVIEW_ANSWERS_COMPREHENSIVE_KEY.md)

## Topic 2: Components (Advanced)

### 1. Core Concept
Components are the fundamental building blocks—they manage a view and associated logic.

### 2. Internal Working
```
Component Lifecycle:
1. Create instance
2. Set input properties
3. ngOnInit
4. Detect changes
5. Update template
6. ngOnDestroy cleanup
```

### 3. Real-world Use
- Netflix: Each piece of UI is a reusable component
- Google: Compose complex interfaces from simple components

### 4. Code Examples

```typescript
// Input/Output pattern
@Component({
  selector: 'app-button',
  template: '<button (click)="onClick()">{{ label }}</button>'
})
export class ButtonComponent {
  @Input() label: string;
  @Output() click = new EventEmitter<void>();
  
  onClick() {
    this.click.emit();
  }
}

// Smart vs Presentational (Container vs Dumb)
// Smart: Connected to data, services
// Presentational: Pure input/output, testable

@Component({
  selector: 'app-user-container',
  template: '<app-user [user]="user$ | async"></app-user>'
})
export class UserContainerComponent {
  user$ = this.userService.getUser();
  
  constructor(private userService: UserService) { }
}

@Component({
  selector: 'app-user',
  template: '<h1>{{ user.name }}</h1>'
})
export class UserComponent {
  @Input() user: User;
}

// ViewChild for accessing child components
@Component({
  selector: 'app-form',
  template: '<input #nameInput />'
})
export class FormComponent {
  @ViewChild('nameInput') nameInput: ElementRef;
  
  ngAfterViewInit() {
    this.nameInput.nativeElement.focus();
  }
}

// ContentProjection (ng-content)
@Component({
  selector: 'app-card',
  template: `
    <div class="card">
      <ng-content select="[cardHeader]"></ng-content>
      <ng-content></ng-content>
      <ng-content select="[cardFooter]"></ng-content>
    </div>
  `
})
export class CardComponent { }

// Usage
<app-card>
  <div cardHeader>Header</div>
  <div>Content</div>
  <div cardFooter>Footer</div>
</app-card>
```

### 5. Interview Questions
1. **What's the difference between ViewChild and ContentChild?**
2. **Design a component hierarchy for Reddit (posts, comments, nested).**
3. **How would you optimize component re-rendering?**
4. **Implement a form component that validates child inputs.**
5. **When would you use ng-content vs creating separate components?**

---

## Topic 3: Change Detection (Critical for Performance)

### 1. Core Concept
**Change detection** is how Angular knows when to re-render components. It's one of the most important—and often misunderstood—concepts.

### 2. How It Works
```
Default Strategy: Check EVERYTHING on every event
Zone.js wraps: setTimeout, click, HTTP, Promises

OnPush Strategy: Only check when @Input changes
Massively more efficient for large apps
```

### 3. Real-world Use
- **Netflix**: Uses OnPush for 10k+ components
- **Google Maps**: OnPush for map tiles
- **Stock tickers**: Manual CD for high-frequency updates

### 4. Code Examples

```typescript
// Default vs OnPush
// DEFAULT - checked on every event (100% safe, slower)
@Component({
  selector: 'app-default',
  template: '{{ data }}'
})
export class DefaultComponent {
  @Input() data: any;
}

// OnPush - more efficient, requires discipline
@Component({
  selector: 'app-optimized',
  template: '{{ data | json }}',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OptimizedComponent implements OnChanges {
  @Input() data: any;
  
  constructor(private cdr: ChangeDetectorRef) { }
  
  ngOnChanges() {
    // Only runs when @Input reference changes
    this.cdr.markForCheck(); // Notify Angular
  }
}

// Manual change detection (rare, advanced)
@Component({...})
export class ManualComponent {
  constructor(private cdr: ChangeDetectorRef) { }
  
  riskyAsyncWork() {
    // Detach from normal CD cycle
    this.cdr.detach();
    
    // Heavy computation without triggering CD
    this.computeHeavy();
    
    // When ready, re-attach
    this.cdr.reattach();
  }
}

// ChangeDetectorRef troubleshooting
@Component({...})
export class DiagnosticComponent {
  constructor(private cdr: ChangeDetectorRef) { }
  
  debug() {
    // Check if component is marked for change
    console.log(this.cdr);
    
    // Force immediate check
    this.cdr.detectChanges();
    
    // Mark for next cycle
    this.cdr.markForCheck();
    
    // Stop checking temporarily
    this.cdr.detach();
  }
}

// Performance optimization pattern
@Component({
  selector: 'app-list',
  template: '<app-item *ngFor="let item of items$ | async" [item]="item"></app-item>',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ListComponent {
  items$ = this.itemService.items$; // Observable
  
  constructor(private itemService: ItemService) { }
}

// Each item gets OnPush
@Component({
  selector: 'app-item',
  template: '{{ item.name }}',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ItemComponent {
  @Input() item: Item;
}

// Use trackBy to prevent re-rendering on list changes
@Component({
  template: '<app-item *ngFor="let item of items; trackBy: trackById" [item]="item"></app-item>'
})
export class ListComponent {
  items: Item[];
  
  trackById(index: number, item: Item) {
    return item.id; // Angular knows identity didn't change
  }
}
```

### 5. Interview Questions
1. **Why is change detection a performance bottleneck?**
2. **When should you use OnPush vs Default?**
3. **Design a system with 50,000 components using OnPush.**
4. **How does Zone.js intercept async operations?**
5. **Explain markForCheck vs detectChanges.**

---

## Topic 4: Dependency Injection

### 1. Core Concept
DI is a design pattern where dependencies are provided to objects rather than created by them.

### 2. How It Works
```
1. @Injectable() marks class as service
2. Injector maintains instances
3. Constructor injection occurs at instantiation
4. Hierarchical: Module-level, Component-level
```

### 3. Real-world Use
- **Mock services for testing**
- **Swapping implementations (API mock vs real)**
- **Singleton management (one UserService for all)**

### 4. Code Examples

```typescript
// Injectable decorator
@Injectable({
  providedIn: 'root' // App-wide singleton (tree-shakeable)
})
export class LoggerService {
  log(msg: string) { console.log(msg); }
}

// Hierarchical DI
@NgModule({
  providers: [UserService] // App-wide
})
export class AppModule { }

@Component({
  selector: 'app-user',
  providers: [UserService] // Component-scoped instance
})
export class UserComponent { }

// Factory providers
export function createLogger(isDev: boolean) {
  return isDev ? new DebugLogger() : new ProductionLogger();
}

@NgModule({
  providers: [
    {
      provide: 'Logger',
      useFactory: createLogger,
      deps: ['ENV_CONFIG'] // Non-class dependencies
    }
  ]
})
export class AppModule { }

// Dependency injection in services
@Injectable()
export class UserService {
  constructor(
    private http: HttpClient,
    private logger: LoggerService
  ) { }
}

// Testing with mock
const mockLogger = new MockLoggerService();
const service = new UserService(mockHttp, mockLogger);

// InjectionToken for non-class values
export const API_URL = new InjectionToken<string>('api.url');

@NgModule({
  providers: [
    { provide: API_URL, useValue: 'https://api.example.com' }
  ]
})
export class AppModule { }

// Usage
@Injectable()
export class ApiService {
  constructor(@Inject(API_URL) private apiUrl: string) { }
}

// Provider strategies
@NgModule({
  providers: [
    // useClass - instantiate class
    { provide: Logger, useClass: DebugLogger },
    
    // useValue - provide existing value
    { provide: API_URL, useValue: 'https://...' },
    
    // useFactory - create using function
    { provide: Logger, useFactory: createLogger },
    
    // useExisting - alias to another provider
    { provide: OldLogger, useExisting: Logger }
  ]
})
export class AppModule { }
```

### 5. Interview Questions
1. **Explain hierarchical dependency injection.**
2. **When would you use providedIn:'root' vs module providers?**
3. **Design a mock service system for testing.**
4. **How would you prevent circular dependencies?**
5. **Implement a service locator pattern (anti-pattern in Angular).**

---

## Topic 5: RxJS Observables (Essential for Production)

### 1. Core Concept
**Observables** are lazy collections of events over time. They enable reactive programming.

### 2. How It Works
```
Observer subscribes to Observable
Observable emits values over time
Observer reacts to each value (next, error, complete)
Subscription manages lifecycle
```

### 3. Real-world Use
- **Netflix**: Tracks user interactions as event streams
- **Google Maps**: Real-time location updates
- **Stripe**: Payment processing events
- **Chat apps**: Message streams

### 4. Code Examples

```typescript
// Basic observable
const observable = new Observable(subscriber => {
  subscriber.next(1);
  subscriber.next(2);
  subscriber.next(3);
  subscriber.complete();
});

observable.subscribe(
  value => console.log(value),
  error => console.error(error),
  () => console.log('Complete')
);

// Subject (both Observable and Observer)
const subject = new Subject<number>();

subject.subscribe(v => console.log('Sub 1:', v));
subject.subscribe(v => console.log('Sub 2:', v));

subject.next(1); // Both subscribers receive
subject.next(2);

// BehaviorSubject (emits last value to new subscribers)
const behaviorSubject = new BehaviorSubject(0);

behaviorSubject.subscribe(v => console.log('Sub 1:', v)); // 0
behaviorSubject.next(1);

behaviorSubject.subscribe(v => console.log('Sub 2:', v)); // 1 (gets last value)

// ReplaySubject (replays N values)
const replaySubject = new ReplaySubject(2);
replaySubject.next(1);
replaySubject.next(2);
replaySubject.next(3);

replaySubject.subscribe(v => console.log(v)); // 2, 3 (last 2)

// Operators (transformation)
from([1, 2, 3, 4, 5])
  .pipe(
    filter(x => x % 2 === 0), // Keep evens: 2, 4
    map(x => x * 2), // Double: 4, 8
    take(2) // First 2: 4, 8
  )
  .subscribe(console.log);

// Common patterns
// Pattern 1: HTTP request
@Injectable()
export class UserService {
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>('/api/users');
  }
}

// Component subscription
@Component({...})
export class UserListComponent implements OnInit, OnDestroy {
  users: User[];
  private destroy$ = new Subject<void>();
  
  ngOnInit() {
    this.userService.getUsers()
      .pipe(takeUntil(this.destroy$)) // Unsubscribe on destroy
      .subscribe(users => this.users = users);
  }
  
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// Pattern 2: Handle form input (debounced search)
@Component({
  template: '<input [formControl]="searchCtrl">'
})
export class SearchComponent {
  searchCtrl = new FormControl();
  
  results$ = this.searchCtrl.valueChanges.pipe(
    debounceTime(300), // Wait 300ms without typing
    distinctUntilChanged(), // Only if value changed
    switchMap(query => this.searchService.search(query)), // Cancel previous request
    startWith([]) // Initial value
  );
}

// Pattern 3: Combine multiple observables
const user$ = this.userService.getUser();
const posts$ = this.postsService.getPosts();
const comments$ = this.commentsService.getComments();

combineLatest([user$, posts$, comments$]).pipe(
  map(([user, posts, comments]) => ({
    user, posts, comments
  }))
).subscribe(data => this.data = data);

// Pattern 4: Error handling
this.service.getData()
  .pipe(
    retryWhen(errors =>
      errors.pipe(
        mergeMap((error, index) =>
          index < 3 ? timer(1000 * (index + 1)) : throwError(error)
        )
      )
    ),
    catchError(error => {
      console.error(error);
      return of(defaultValue);
    })
  )
  .subscribe(data => this.data = data);

// Pattern 5: Race conditions prevention
private currentPage = 1;

goToPage(page: number) {
  this.currentPage = page;
  
  this.loadingService.show();
  
  this.dataService.getPage(page)
    .pipe(
      // If user navigates again, cancel this request
      takeUntil(this.pageChanged$)
    )
    .subscribe(
      data => this.data = data,
      error => console.error(error),
      () => this.loadingService.hide()
    );
}
```

### 5. Interview Questions
1. **What's the difference between Subject, BehaviorSubject, and ReplaySubject?**
2. **Why is switchMap better than mergeMap for form searches?**
3. **Design a system that prevents race conditions in Angular.**
4. **Implement a retry mechanism with exponential backoff.**
5. **How would you handle circular dependencies in RxJS chains?**

---

## Topic 6: Lifecycle Hooks

### 1. Core Concept
**Lifecycle hooks** are methods called at specific moments in a component's life.

### 2. Execution Order
```
1. Constructor
2. ngOnChanges (if @Input properties exist)
3. ngOnInit (perfect for initialization)
4. ngDoCheck (change detection runs)
5. ngAfterContentInit
6. ngAfterContentChecked
7. ngAfterViewInit (DOM ready)
8. ngAfterViewChecked
9. ngOnDestroy (cleanup)
```

### 3. Real-world Use
- **Data fetching**: ngOnInit
- **DOM access**: ngAfterViewInit
- **Cleanup**: ngOnDestroy

### 4. Code Examples

```typescript
@Component({...})
export class LifecycleComponent implements 
  OnInit, OnChanges, NgDoCheck, OnDestroy {
  
  @Input() data: any;
  private destroy$ = new Subject<void>();
  
  // Called when @Input properties change
  ngOnChanges(changes: SimpleChanges) {
    changes['data'].previousValue // Old value
    changes['data'].currentValue  // New value
  }
  
  // Called once after first ngOnChanges
  ngOnInit() {
    // Perfect for initialization
    this.dataService.getData()
      .pipe(takeUntil(this.destroy$))
      .subscribe(data => this.data = data);
  }
  
  // Called every change detection cycle
  ngDoCheck() {
    // Implement custom change detection if needed
    // CAUTION: Called very frequently!
  }
  
  // Called when component is about to be destroyed
  ngOnDestroy() {
    // CRITICAL: Unsubscribe from observables
    this.destroy$.next();
    this.destroy$.complete();
  }
}

// Unsubscribe patterns
// Pattern 1: takeUntil (recommended)
ngOnInit() {
  this.obs$.pipe(takeUntil(this.destroy$))
    .subscribe(/* ... */);
}

// Pattern 2: Manual unsubscribe (error-prone)
private subscription: Subscription;

ngOnInit() {
  this.subscription = this.obs$.subscribe(/* ... */);
}

ngOnDestroy() {
  this.subscription.unsubscribe();
}

// Pattern 3: Async pipe (automatic unsubscribe)
template: '{{ data$ | async }}'

// Lifecycle timing diagram
@Component({...})
export class TimingComponent implements AfterViewInit {
  @ViewChild('ref') ref: ElementRef;
  
  ngAfterViewInit() {
    // CORRECT: ref is available now
    console.log(this.ref.nativeElement.textContent);
  }
  
  ngOnInit() {
    // WRONG: ref not available yet
    console.log(this.ref); // undefined
  }
}
```

### 5. Interview Questions
1. **When should you initialize data in ngOnInit vs constructor?**
2. **Why is memory leak prevention critical in ngOnDestroy?**
3. **Implement a component that removes event listeners on destroy.**
4. **Design a pattern to prevent subscription leaks.**
5. **When would you use ngDoCheck?**

---

## Topic 7-14: Advanced Topics (Condensed)

### Topic 7: Routing
```typescript
const routes = [
  { path: '', redirectTo: 'home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'users/:id', component: UserComponent },
  { path: 'admin', component: AdminComponent, canActivate: [AuthGuard] },
  {
    path: 'profile',
    component: ProfileComponent,
    data: { title: 'Profile' }
  },
  // Lazy loading - separate bundle
  {
    path: 'shop',
    loadChildren: () => import('./shop/shop.module').then(m => m.ShopModule)
  }
];

// Guard example
@Injectable()
export class AuthGuard implements CanActivate {
  canActivate(route: ActivatedRouteSnapshot): boolean {
    return this.authService.isAuthenticated();
  }
}

// Accessing route params
@Component({...})
export class UserComponent {
  userId$ = this.route.params.pipe(map(p => p.id));
  
  constructor(private route: ActivatedRoute) { }
}
```

**Key Patterns**:
- Lazy loading reduces initial bundle
- Guards prevent unauthorized access
- Route data passes metadata

### Topic 8: State Management

```typescript
// NgRx pattern (Redux for Angular)
@Injectable()
export class UserStore extends ComponentStore<UserState> {
  users$ = this.select(state => state.users);
  
  loadUsers = this.effect((trigger$: Observable<void>) =>
    trigger$.pipe(
      switchMap(() =>
        this.userService.getUsers().pipe(
          tapResponse(
            users => this.patchState({ users }),
            error => this.handleError(error)
          )
        )
      )
    )
  );
}

// Local component state (simpler alternative)
@Component({...})
export class UserListComponent {
  private userSubject = new BehaviorSubject<User[]>([]);
  users$ = this.userSubject.asObservable();
  
  loadUsers() {
    this.userService.getUsers()
      .subscribe(users => this.userSubject.next(users));
  }
}
```

**Key Points**:
- Centralized state management for large apps
- NgRx/Akita for complex scenarios
- LocalBS for simple component state

### Topic 9: Lazy Loading

```typescript
// Feature module with lazy loading
// In app routing
{
  path: 'posts',
  loadChildren: () => import('./posts/posts.module')
    .then(m => m.PostsModule)
}

// posts.module.ts
@NgModule({
  declarations: [PostListComponent, PostDetailComponent],
  imports: [
    CommonModule,
    RouterModule.forChild([
      { path: '', component: PostListComponent },
      { path: ':id', component: PostDetailComponent }
    ])
  ]
})
export class PostsModule { }

// Effect: Only loaded when user navigates to /posts
```

**Benefits**:
- Smaller initial bundle (faster load)
- Code splitting automatic
- Scale to 100s of routes

### Topic 10: Performance Optimization

```typescript
// Optimization techniques

// 1. Change detection optimization
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})

// 2. TrackBy in ngFor
<div *ngFor="let item of items; trackBy: trackById">
  {{ item.name }}
</div>

trackById(index: number, item: Item) {
  return item.id;
}

// 3. Lazy load images
<img loading="lazy" src="...">

// 4. Virtual scrolling for long lists
<cdk-virtual-scroll-viewport itemSize="50">
  <div *cdkVirtualFor="let item of items">
    {{ item.name }}
  </div>
</cdk-virtual-scroll-viewport>

// 5. Code splitting
const routes = [{
  path: 'heavy',
  loadComponent: () => import('./heavy.component')
    .then(m => m.HeavyComponent)
}];
```

### Topic 11: Forms (Reactive vs Template)

```typescript
// Template-driven (simple, two-way binding)
<form #form="ngForm">
  <input [(ngModel)]="user.name">
  <button (click)="submit(form.value)">Submit</button>
</form>

// Reactive (testable, scalable)
@Component({...})
export class FormComponent implements OnInit {
  form: FormGroup;
  
  ngOnInit() {
    this.form = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      address: this.fb.group({
        street: [''],
        city: ['']
      })
    });
  }
  
  submit() {
    if(this.form.valid) {
      this.userService.create(this.form.value).subscribe();
    }
  }
}

// Custom validator
function notAdminEmail(control: AbstractControl) {
  if(!control.value) return null;
  
  if(control.value.includes('admin')) {
    return { adminEmail: true };
  }
  
  return null;
}

// Usage
email: ['', [Validators.required, notAdminEmail]]
```

**Recommendation**: Use Reactive for production apps (testable, type-safe).

### Topic 12: HttpClient

```typescript
@Injectable()
export class ApiService {
  constructor(private http: HttpClient) { }
  
  // Interceptor for headers, errors
  get<T>(url: string): Observable<T> {
    return this.http.get<T>(url).pipe(
      tap(() => this.loadingService.hide()),
      catchError(error => {
        this.errorService.handle(error);
        return throwError(error);
      })
    );
  }
}

// HTTP Interceptor
@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // Add auth token to all requests
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${this.authService.token}`
      }
    });
    
    return next.handle(authReq);
  }
}

// Register
providers: [
  { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
]
```

### Topic 13: Angular vs React Comparison

| Aspect | Angular | React |
|--------|---------|-------|
| **Architecture** | Full framework | Library (UI only) |
| **Language** | TypeScript (required) | JavaScript/TypeScript (optional) |
| **Learning Curve** | Steep | Gradual |
| **Data Binding** | Two-way (ngModel) | One-way (props) |
| **Performance** | Good (OnPush) | Excellent (virtual DOM) |
| **Testing** | Built-in DI | Setup needed |
| **Bundle Size** | ~1.2MB | ~50KB (React core) |
| **Enterprise** | Strong | Growing |

**When to use Angular**:
- Large enterprise apps
- Need full framework
- Strong architecture requirements
- Complex forms

**When to use React**:
- Highly interactive UIs
- Flexibility needed
- Smaller bundle critical
- Learning project

### Topic 14: Security Best Practices

```typescript
// 1. XSS Prevention
// WRONG
<div [innerHTML]="userInput"></div>

// RIGHT
<div>{{ userInput }}</div> // Auto-escaped

// For trusted HTML, sanitize
import { DomSanitizer } from '@angular/platform-browser';

constructor(private sanitizer: DomSanitizer) { }

getSafeHtml() {
  return this.sanitizer.sanitize(
    SecurityContext.HTML,
    userInput
  );
}

// 2. CSRF Prevention
// Angular adds XSRF token automatically
// Interceptor: setHeaders: { 'X-XSRF-TOKEN': token }

// 3. Avoid storing sensitive data
// WRONG
localStorage.setItem('token', authToken);

// RIGHT - store in httpOnly cookie or memory

// 4. Content Security Policy
// In index.html or server headers
<meta http-equiv="Content-Security-Policy"
  content="default-src 'self'">
```

---

## Real-World Application Examples

### Example: E-commerce Platform

**Architecture**:
```
App Module
├── Core Module (singleton services)
│   ├── AuthService
│   ├── HttpService
│   └── Logger
├── Shared Module (reusable)
│   ├── CommonModule
│   ├── HttpClient
│   ├── Button Component
│   └── Card Component
└── Feature Modules (lazy-loaded)
    ├── Product Module
    │   ├── ProductListComponent
    │   ├── ProductDetailComponent
    │   └── ProductService
    ├── Cart Module
    │   ├── CartComponent
    │   └── CartService
    └── Checkout Module
        ├── CheckoutComponent
        ├── PaymentComponent
        └── OrderService
```

### Example: Real-time Chat App

**Key Technologies**:
- **WebSocket** for real-time messaging
- **RxJS Subjects** for message streams
- **Local state management** with BehaviorSubject
- **Lazy-loaded chat modules** per channel

```typescript
@Injectable()
export class ChatService {
  private messagesSubject = new BehaviorSubject<Message[]>([]);
  messages$ = this.messagesSubject.asObservable();
  
  constructor(private ws: WebSocketService) {
    this.ws.connect().subscribe(msg => {
      const current = this.messagesSubject.value;
      this.messagesSubject.next([...current, msg]);
    });
  }
  
  sendMessage(msg: Message) {
    this.ws.send(msg);
  }
}
```

---

## Architecture Diagrams

### Change Detection Flow
```
Event (click, HTTP, Timer)
    ↓
Zone.js catches event
    ↓
Angular notifies ChangeDetectorRef
    ↓
RunChangeDetection scheduler
    ↓
Walk component tree
    ↓
OnPush ? Check only if @Input changed : Always check
    ↓
Run ngDoCheck hooks
    ↓
Compare old vs new bindings
    ↓
If different, call ngAfterContentChecked
    ↓
Call ngAfterViewChecked
    ↓
Update DOM (if changed)
    ↓
Render
```

### Dependency Injection
```
AppModule
├── Provides: UserService (singleton)
│   └── Requests: HttpClient (from BrowserModule)
│
Feature Module
├── Provides: FeatureService (module-scoped)
│   └── Requests: UserService (uses app-level)
│
Component
├── Provides: ComponentService (instance-scoped, new per component)
│   └── Requests: both module and app services

Resolution: Most specific wins
Component > Module > App
```

### Observable Pattern Flow
```
Component
    ↓
Calls UserService.getUsers()
    ↓
Returns Hot Observable (HTTP)
    ↓
Component subscribes
    ↓
Observable emits value (data arrives)
    ↓
Subscription's next() called
    ↓
Component updates view
```

---

## System Design Considerations

### Scalability
- **Single file limitation**: Break >500 line components
- **Module count**: Organize 100+ modules by domain
- **Shared services**: Singleton prevents memory waste
- **Lazy loading**: Load 50MB app as 5MB chunks

### Performance
- **Change detection**: OnPush can reduce cycles by 90%
- **Memory**: Unsubscribe to prevent leaks
- **Bundle**: Code splitting can reduce initial load 80%
- **Rendering**: Virtual scrolling for 10k+ items

### Maintainability
- **Clear hierarchy**: Shared, Core, Features structure
- **Type safety**: TypeScript catches bugs precompile
- **DI**: Testable, mockable components
- **Reactive**: RxJS flows beat imperative code

---

## 10 Hardest Interview Questions

### JavaScript
1. Explain event loop starvation. Design a system that prevents it.
2. Implement a memory-efficient closure system for 1M counters.
3. Design deep vs shallow copy for objects with circular references.
4. Explain garbage collection phases and how to prevent memory leaks.
5. Implement async/await with concurrency limits.
6. Design functional programming for data transformation pipelines.
7. Explain prototype chain performance implications at scale.
8. Implement debounce/throttle with request cancellation.
9. Design execution context management for recursive algorithms.
10. Explain Promise state transitions and race condition prevention.

### Angular
1. Design state management for 100+ feature modules.
2. Optimize change detection for 10k+ components.
3. Implement lazy loading and preloading strategy.
4. Design RxJS pattern for preventing memory leaks.
5. Implement custom form validator with async validation.
6. Design security model (XSS, CSRF, CSP).
7. Create performant infinite scroll with virtual scrolling.
8. Implement advanced routing with parameters and guards.
9. Design dependency injection for testing complex scenarios.
10. Create architecture that grows from 10 to 1000 components.

---

## 2-Day Study Plan

### Day 1: JavaScript Fundamentals (4-5 hours)
1. Execution Context (30 min) - understand your code's environment
2. Event Loop (45 min) - master async execution
3. Call Stack (15 min) - quick concept
4. Closures (1 hour) - critical for production code
5. Prototypes (45 min) - understand inheritance
6. This Keyword (45 min) - fixing bugs caused by this
7. Promises (1 hour) - asyncreturn without callbacks
8. Async/Await (30 min) - syntax sugar mastery
9. Memory Management (45 min) - prevent leaks in production
10. Debounce/Throttle (30 min) - optimize events
11. Deep vs Shallow Copy (30 min) - mutation problems
12. Functional Programming (1 hour) - composition and purity

### Day 2: Angular Mastery (4-5 hours)
1. Architecture (45 min) - big picture
2. Components (1 hour) - building blocks
3. Change Detection (1 hour) - critical for performance  
4. DI (30 min) - testability
5. RxJS Observables (1 hour) - reactive streams
6. Lifecycle Hooks (30 min) - cleanup and timing
7. Routing (45 min) - navigation
8. State Management (45 min) - complex apps
9. Performance (45 min) - optimization techniques
10. Forms (30 min) - reactive forms
11. Security (30 min) - XSS/CSRF prevention
12. Angular vs React (15 min) - context

---

## Quick Reference Cheat Sheet

### JavaScript
- **Execution Context**: Variable hoisting, scope chain, this binding
- **Event Loop**: Microtasks ≫ Macrotasks, causes starvation
- **Closures**: Functions capture outer scope, enable data privacy
- **Prototypes**: Every object has [[Prototype]], enables inheritance
- **This**: Determined at call-time, not definition-time
- **Promises**: Immutable state transitions, chaining
- **Async/Await**: Syntactic sugar over Promises
- **Memory**: Garbage collection, reference cycles, WeakMap
- **Debounce**: Delay execution until after inactivity
- **Throttle**: Execute at most once per interval
- **Deep Copy**: Full recursive copy, expensive
- **Functional**: Pure functions, immutability, composition

### Angular
- **Modules**: Container for features and dependencies
- **Components**: UI building blocks with lifecycle
- **Change Detection**: Determines when to re-render (OnPush optimization)
- **DI**: Dependency Injection enables testability
- **Observables**: Async streams, RxJS operators
- **Lifecycle**: ngOnInit, ngDestroy, ngAfterViewInit
- **Routing**: Lazy loading, guards, navigation
- **State**: NgRx, Akita for complex apps
- **Reactive Forms**: FormGroup, validators, custom logic
- **Templates**: {{}} interpolation, [property] binding, (event) handlers
- **Services**: Business logic, HTTP, data management
- **Directives**: Structural (*ngIf, *ngFor), attribute

---

## Final Tips for Interview Success

1. **Show system design thinking** - don't just write code, explain trade-offs
2. **Mention performance** - production systems care about scale
3. **Ask about constraints** - before designing, clarify requirements
4. **Draw diagrams** - visualize architecture, flows, interactions
5. **Discuss security** - XSS, CSRF, data protection
6. **Consider testing** - how would you test this?
7. **Think maintenance** - how does this scale with the team?
8. **Explain trade-offs** - no perfect solution, balance concerns
9. **Compare alternatives** - why this approach vs others?
10. **Code like production** - error handling, edge cases, monitoring

Good luck! You've got this.
