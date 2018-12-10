import { Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import { Request } from "../../core/models/Request";
import { RequestsService } from "../../core/services/requests.service";
import { Subscription } from "rxjs";
import { MatSort, MatTableDataSource} from "@angular/material";

@Component({
  selector: 'app-requests-overview',
  templateUrl: './requests-overview.component.html',
  styleUrls: ['./requests-overview.component.scss']
})
export class RequestsOverviewComponent implements OnInit, OnDestroy {

  constructor(private requestsService: RequestsService) { }

  @Input()
  requests: Request[];


  @ViewChild(MatSort) set matSort(ms: MatSort) {
    this.sort = ms;
    this.setDataSource();
  }

  private requestsSubscription: Subscription;
  private sort: MatSort;

  loading = true;

  displayedColumns: string[] = ['id', 'facilityId', 'status', 'action'];
  dataSource: MatTableDataSource<Request>;

  setDataSource() {
    if (!!this.dataSource) {
      this.dataSource.sort = this.sort;
    }
  }

  ngOnInit() {
    this.requestsSubscription = this.requestsService.getAllRequests().subscribe(requests => {
      this.loading = false;
      this.requests = requests;
      this.dataSource = new MatTableDataSource<Request>(requests);
    });
  }

  ngOnDestroy() {
    this.requestsSubscription.unsubscribe();
  }
}
