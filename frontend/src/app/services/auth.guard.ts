import {Injectable} from '@angular/core';
import {CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router} from '@angular/router';
import {Observable} from 'rxjs';
import {AuthService} from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {
  }

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean> | Promise<boolean> | boolean {
    const currentUser = this.authService.currentUserValue;
    const expectedRole = next.data['role'];

    if (currentUser && currentUser.roles.some((role: { name: string }) => role.name === expectedRole)) {
      return true;
    }

    if (currentUser) {
      alert('Доступ на цю сторінку вам заборонено');
    }

    this.router.navigate(['/login']);
    return false;
  }
}
