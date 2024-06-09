import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {LoginComponent} from './component/login/login.component';
import {RegisterComponent} from './component/register/register.component';
import {AdminCabinetComponent} from './component/admin-cabinet/admin-cabinet.component';
import {AdminCreateVotingComponent} from './component/admin-create-voting/admin-create-voting.component';
import {AdminHomeUsersComponent} from './component/admin-home-users/admin-home-users.component';
import {AdminHomeVotingsComponent} from './component/admin-home-votings/admin-home-votings.component';
import {AdminUserDetailsComponent} from './component/admin-user-details/admin-user-details.component';
import {UserHomeComponent} from './component/user-home/user-home.component';
import {UserProfileComponent} from './component/user-profile/user-profile.component';
import {VotingDetailsComponent} from './component/voting-details/voting-details.component';
import {VotingDetailsAdmComponent} from './component/voting-details-adm/voting-details-adm.component';
import {AuthGuard} from './services/auth.guard';
import {RestorePasswordComponent} from "./component/restore-password/restore-password.component";
import {SearchResultsComponent} from './component/search-results/search-results.component';
import {DatePipe} from "@angular/common";
import {SearchAdmResultsComponent} from "./component/search-adm-results/search-adm-results.component";

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'restore-password', component: RestorePasswordComponent },
  { path: 'admin-cabinet', component: AdminCabinetComponent, canActivate: [AuthGuard], data: { role: 'administrator' } },
  { path: 'admin-create-voting', component: AdminCreateVotingComponent, canActivate: [AuthGuard], data: { role: 'administrator' } },
  { path: 'admin-home-users', component: AdminHomeUsersComponent, canActivate: [AuthGuard], data: { role: 'administrator' } },
  { path: 'admin-home-votings', component: AdminHomeVotingsComponent, canActivate: [AuthGuard], data: { role: 'administrator' } },
  { path: 'admin-user-details/:id', component: AdminUserDetailsComponent, canActivate: [AuthGuard], data: { role: 'administrator' } },
  { path: 'user-home', component: UserHomeComponent, canActivate: [AuthGuard], data: { role: 'user' } },
  { path: 'user-profile', component: UserProfileComponent, canActivate: [AuthGuard], data: { role: 'user' } },
  { path: 'search-results', component: SearchResultsComponent, canActivate: [AuthGuard], data: { role: 'user' } },
  { path: 'search-adm-results', component: SearchAdmResultsComponent, canActivate: [AuthGuard], data: { role: 'administrator' } },
  { path: 'voting-details/:id', component: VotingDetailsComponent, canActivate: [AuthGuard], data: { role: 'user' } },
  { path: 'voting-details-adm/:id', component: VotingDetailsAdmComponent, canActivate: [AuthGuard], data: { role: 'administrator' } }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  providers: [DatePipe],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
