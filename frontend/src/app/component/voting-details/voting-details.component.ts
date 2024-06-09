import {Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {ActivatedRoute, Router, RouterModule} from '@angular/router';
import {AuthService} from '../../services/auth.service';
import {VotingService} from '../../services/voting.service';
import {CandidateService} from '../../services/candidate.service';
import {VoteResultService} from '../../services/vote-result.service';
import {VOTING_CATEGORIES} from '../../constants/voting-categories';
import {CommonModule, DatePipe} from '@angular/common';
import {ImageDto} from '../../models/image-dto.model';
import {VoteResult} from '../../models/vote-result.model';
import {FormsModule} from "@angular/forms";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from "../../../environments/environment";

@Component({
  selector: 'app-voting-details',
  templateUrl: './voting-details.component.html',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    DatePipe,
    FormsModule
  ],
  styleUrls: ['./voting-details.component.css']
})

export class VotingDetailsComponent implements OnInit {
  currentTime: string;
  user: any;
  userRole: string;
  voting: any;
  candidateImages: { [key: number]: string } = {};
  isModalOpen: boolean = false;
  modalImage: string = '';
  selectedCandidateId: number = 0;
  hasVoted: boolean = false;
  verificationStatus: string = '';
  isLoading: boolean = true;
  searchQuery: string = '';
  voteResults: any[] = [];
  publicUserIds: string[] = [];

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private votingService: VotingService,
    private candidateService: CandidateService,
    private voteResultService: VoteResultService,
    private datePipe: DatePipe,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {
    this.currentTime = '';
    this.user = {};
    this.userRole = '';
    this.voting = {};
  }

  ngOnInit(): void {
    this.updateCurrentTime();
    setInterval(() => {
      this.updateCurrentTime();
    }, 60000);

    this.authService.currentUser.subscribe(user => {
      this.user = user;
      if (this.user) {
        this.fetchVerificationStatus();
      }
    });

    this.route.params.subscribe(params => {
      const votingId = +params['id'];
      this.getVotingDetails(votingId.toString());
    });
  }

  checkIfUserHasVoted(): void {
    if (this.user && this.voting) {
      this.voteResultService.hasUserVoted(this.user.id, this.voting.id).subscribe(
        (hasVoted: boolean) => {
          this.hasVoted = hasVoted;
          this.cdr.detectChanges();
        },
        (error) => {
          console.error('Error checking if user has voted', error);
        }
      );
    }
  }

  updateCurrentTime(): void {
    this.currentTime = this.datePipe.transform(new Date(), 'dd.MM.yyyy HH:mm') || '';
  }

  getVotingDetails(votingId: string): void {
    this.votingService.getVotingById(votingId).subscribe((voting: any) => {
      this.voting = voting;
      this.loadCandidateImages();
      this.checkIfUserHasVoted();
      if (this.voting.status === 'COMPLETED') {
        this.loadVoteResults(votingId);
      }
    });
  }

  loadVoteResults(votingId: string): void {
    this.voteResultService.getShowVoteResults(votingId).subscribe(
      (data: any) => {
        this.voteResults = data.candidatePercentages;
        this.publicUserIds = data.userPublicIds;
        this.cdr.detectChanges();
      },
      (error) => {
        console.error('Error loading vote results', error);
      }
    );
  }

  getVotingImage(): string {
    return this.voting.category === VOTING_CATEGORIES.UNIVERSITY ? 'assets/university_big.png' : 'assets/group_big.png';
  }

  loadCandidateImages(): void {
    this.candidateService.getCandidateImages().subscribe(
      (images: ImageDto[]) => {
        this.candidateImages = {};
        images.forEach(image => {
          this.candidateImages[image.id] = 'data:image/jpeg;base64,' + image.image;
        });
      },
      (error) => {
        console.error('Error loading candidate images', error);
      }
    );
  }

  getDisplayStatus(status: string): string {
    const statusMap: { [key: string]: string } = {
      'SCHEDULED': 'Планується',
      'ACTIVE': 'Активне',
      'COMPLETED': 'Завершене',
      'SUSPENDED': 'Призупинено'
    };
    return statusMap[status] || status;
  }

  getStatusLabelClass(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'active';
      case 'COMPLETED':
        return 'finished';
      case 'SUSPENDED':
        return 'suspended';
      case 'SCHEDULED':
        return 'scheduled';
      default:
        return '';
    }
  }

  openModal(image: string): void {
    this.modalImage = image;
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
    this.modalImage = '';
  }

  submitVote(): void {
    const voteResult: VoteResult = {
      votingId: this.voting.id,
      userId: this.user.id,
      candidateId: this.selectedCandidateId
    };

    this.voteResultService.createVoteResult(voteResult).subscribe(
      () => {
        alert('Ваш голос успішно збережено!');
        this.router.navigate(['/user-home']);
      },
      (error) => {
        console.error('Error submitting vote', error);
        alert('Сталася помилка під час збереження вашого голосу: ' + error.message);
      }
    );
  }

  fetchVerificationStatus(): void {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${this.authService.currentUserValue.token}`);
    this.http.get(`${environment.baseUrl}/users/${this.user.id}/status`, {headers})
      .subscribe(
        (response: any) => {
          this.verificationStatus = response;
          this.isLoading = false;
          this.cdr.detectChanges();
        },
        (error: any) => {
          console.error('Помилка завантаження статусу верифікації', error);
          this.isLoading = false;
        }
      );
  }

  onSearch(): void {
    this.router.navigate(['/search-results'], {queryParams: {query: this.searchQuery}});
  }

  logout(): void {
    this.authService.logout();
  }
}
