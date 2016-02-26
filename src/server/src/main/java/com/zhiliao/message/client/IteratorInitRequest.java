package com.zhiliao.message.client;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;
import java.util.Set;

public class IteratorInitRequest extends IteratorRequest {
	@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS)
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	public static class IteratorSpecification {
	}

	public static class ByBranchIteratorSpecification extends  IteratorSpecification {
		private long branch;

		public long getBranch() {
			return branch;
		}

		public void setBranch(long branch) {
			this.branch = branch;
		}

		@Override
		public String toString() {
			return "ByBranchIteratorSpecification{" +
					"branch=" + branch +
					'}';
		}
	}

	public static class ByTagIteratorSpecification extends  IteratorSpecification {
		private Set<String> tags;

		public Set<String> getTags() {
			return tags;
		}

		@Override
		public String toString() {
			return "ByTagIteratorSpecification{" +
					"tags=" + tags +
					"} " + super.toString();
		}

		public void setTags(Set<String> tags) {
			this.tags = tags;
		}
	}

	public static class ByFollowIteratorSpecification extends IteratorSpecification {
		private Long follower;
		private Long followed;

		public Long getFollower() {
			return follower;
		}

		public void setFollower(Long follower) {
			this.follower = follower;
		}

		public Long getFollowed() {
			return followed;
		}

		public void setFollowed(Long followed) {
			this.followed = followed;
		}
	}

	public IteratorInitRequest(long mark, long id, IteratorSpecification specification) {
		super(mark, id);
		this.specification = specification;
	}

	public IteratorInitRequest() {

	}

	private IteratorSpecification specification;

	public IteratorSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(IteratorSpecification specification) {
		this.specification = specification;
	}

	@Override
	public String toString() {
		return "IteratorInitRequest{" +
				"specification=" + specification +
				"} " + super.toString();
	}
}
