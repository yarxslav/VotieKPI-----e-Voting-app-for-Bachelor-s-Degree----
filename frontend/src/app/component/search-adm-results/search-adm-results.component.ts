import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, RouterLink, Router} from '@angular/router';
import {VotingService} from '../../services/voting.service';
import {Voting} from "../../models/voting.model";
import {NgClass, NgForOf, NgIf} from "@angular/common";
import {User} from "../../models/user.model";
import {AuthService} from "../../services/auth.service";
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-search-adm-results',
  templateUrl: './search-adm-results.component.html',
  styleUrls: ['./search-adm-results.component.css'],
  standalone: true,
  imports: [
    RouterLink,
    NgForOf,
    NgIf,
    NgClass,
    FormsModule
  ],
})

export class SearchAdmResultsComponent implements OnInit {
  searchResults: Voting[] = [];
  currentTime: string;
  user: User | null;
  userRole: string;
  searchQuery: string = '';

  constructor(
    private route: ActivatedRoute,
    private votingService: VotingService,
    private authService: AuthService,
    private router: Router
  ) {
    this.currentTime = '';
    this.user = null;
    this.userRole = '';
  }

  ngOnInit(): void {
    this.updateCurrentTime();
    setInterval(() => this.updateCurrentTime(), 60000);

    this.authService.currentUser.subscribe((user: User) => {
      this.user = user;
      this.userRole = user?.roles[0]?.name || '';
    });

    this.route.queryParams.subscribe(params => {
      const query = params['query'];
      if (query) {
        this.searchVotings(query);
      }
    });
  }

  searchVotings(query: string): void {
    this.votingService.searchByPublicId(query).subscribe((results: Voting[]) => {
      this.searchResults = results;
    });
  }

  updateCurrentTime(): void {
    const now = new Date();
    this.currentTime = now.toLocaleString('uk-UA', {
      hour: '2-digit',
      minute: '2-digit',
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  logout(): void {
    this.authService.logout();
  }

  getStatusTranslation(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Активне';
      case 'COMPLETED':
        return 'Завершене';
      case 'SUSPENDED':
        return 'Призупинено';
      case 'SCHEDULED':
        return 'Планується';
      default:
        return status;
    }
  }

  getImageSrc(type: string): string {
    return type === 'Групове' ? 'assets/group.png' : 'assets/university.png';
  }

  getVotingTitle(voting: Voting): string {
    if (voting.category === 'Групове') {
      return `${voting.group} | ${voting.name}`;
    }
    return voting.name;
  }

  onSearch(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/search-adm-results'], { queryParams: { query: this.searchQuery } });
  }

  getStatusLabelClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'active-label';
      case 'COMPLETED':
        return 'finished-label';
      case 'SUSPENDED':
        return 'suspended-label';
      case 'SCHEDULED':
        return 'scheduled-label';
      default:
        return '';
    }
  }
}
