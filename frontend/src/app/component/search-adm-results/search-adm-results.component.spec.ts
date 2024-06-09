import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchAdmResultsComponent } from './search-adm-results.component';

describe('SearchAdmResultsComponent', () => {
  let component: SearchAdmResultsComponent;
  let fixture: ComponentFixture<SearchAdmResultsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SearchAdmResultsComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(SearchAdmResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
