import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { LoginComponent } from './component/login/login.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    LoginComponent,
    RouterModule,
  ],
  templateUrl: './app.component.html'
})
export class AppComponent {
  title = 'Student e-Vote';
}
