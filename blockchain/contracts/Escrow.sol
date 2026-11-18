
 // SPDX-License-Identifier: MIT
 pragma solidity ^0.8.24;

 contract Escrow {
     address public immutable client;
     address payable public immutable freelancer;
     address public immutable arbitrator;

     uint256 public immutable amount;

     bool public funded;
     bool public submitted;
     bool public approved;
     bool public released;
     bool public refunded;
     bool public disputed;

     enum Status {
         Created,
         Funded,
         Submitted,
         Approved,
         Released,
         Refunded,
         Disputed
     }

     Status public status;

     event Deposited(address indexed client, uint256 amount);
     event WorkSubmitted();
     event MilestoneApproved();
     event PaymentReleased(address indexed freelancer, uint256 amount);
     event Refunded(address indexed client, uint256 amount);
     event DisputeRaised(address indexed raisedBy);
     event DisputeResolved(bool releaseToFreelancer);

     modifier onlyClient() {
         require(msg.sender == client, "Only client");
         _;
     }

     modifier onlyFreelancer() {
         require(msg.sender == freelancer, "Only freelancer");
         _;
     }

     modifier onlyArbitrator() {
         require(msg.sender == arbitrator, "Only arbitrator");
         _;
     }

     constructor(
         address payable _freelancer,
         address _arbitrator,
         uint256 _amount
     ) {
         require(_freelancer != address(0), "Invalid freelancer");
         require(_arbitrator != address(0), "Invalid arbitrator");
         require(_freelancer != msg.sender, "Client cannot be freelancer");
         require(_arbitrator != msg.sender, "Client cannot be arbitrator");
         require(_arbitrator != _freelancer, "Invalid arbitrator");
         require(_amount > 0, "Amount must be positive");

         client = msg.sender;
         freelancer = _freelancer;
         arbitrator = _arbitrator;
         amount = _amount;
         status = Status.Created;
     }

     function deposit() external payable onlyClient {
         require(!funded, "Already funded");
         require(msg.value == amount, "Incorrect deposit amount");

         funded = true;
         status = Status.Funded;

         emit Deposited(msg.sender, msg.value);
     }

     function submitMilestone() external onlyFreelancer {
         require(funded, "Not funded");
         require(!submitted, "Already submitted");
         require(!disputed, "Dispute active");
         require(!released && !refunded, "Escrow closed");

         submitted = true;
         status = Status.Submitted;

         emit WorkSubmitted();
     }

     function approveMilestone() external onlyClient {
         require(submitted, "Work not submitted");
         require(!disputed, "Dispute active");
         require(!released && !refunded, "Escrow closed");

         approved = true;
         status = Status.Approved;

         emit MilestoneApproved();
     }

     function releasePayment() external onlyClient {
         require(funded, "Not funded");
         require(submitted && approved, "Approval required");
         require(!disputed, "Dispute active");
         require(!released && !refunded, "Escrow closed");

         released = true;
         status = Status.Released;

         (bool success, ) = freelancer.call{value: amount}("");
         require(success, "Transfer failed");

         emit PaymentReleased(freelancer, amount);
     }

     function refund() external onlyClient {
         require(funded, "Not funded");
         require(!submitted, "Work already submitted");
         require(!disputed, "Dispute active");
         require(!released && !refunded, "Escrow closed");

         refunded = true;
         status = Status.Refunded;

         (bool success, ) = payable(client).call{value: amount}("");
         require(success, "Refund failed");

         emit Refunded(client, amount);
     }

     function raiseDispute() external {
         require(msg.sender == client || msg.sender == freelancer,
             "Not a participant");
         require(funded, "Not funded");
         require(!released && !refunded, "Escrow closed");
         require(!disputed, "Already disputed");

         disputed = true;
         status = Status.Disputed;

         emit DisputeRaised(msg.sender);
     }

     function resolveDispute(bool releaseToFreelancer)
         external
         onlyArbitrator
     {
         require(disputed, "No active dispute");
         require(!released && !refunded, "Escrow closed");

         disputed = false;

         if (releaseToFreelancer) {
             released = true;
             status = Status.Released;

             (bool success, ) =
                 freelancer.call{value: amount}("");
             require(success, "Transfer failed");
         } else {
             refunded = true;
             status = Status.Refunded;

             (bool success, ) =
                 payable(client).call{value: amount}("");
             require(success, "Refund failed");
         }

         emit DisputeResolved(releaseToFreelancer);
     }
 }
