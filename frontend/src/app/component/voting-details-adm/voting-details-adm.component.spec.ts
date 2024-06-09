import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VotingDetailsAdmComponent } from './voting-details-adm.component';

describe('VotingDetailsAdmComponent', () => {
  let component: VotingDetailsAdmComponent;
  let fixture: ComponentFixture<VotingDetailsAdmComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VotingDetailsAdmComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(VotingDetailsAdmComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
