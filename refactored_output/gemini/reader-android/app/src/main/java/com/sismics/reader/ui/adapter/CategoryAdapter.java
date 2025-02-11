```java
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<String> categories;
    private final CategoryManager categoryManager;

    public CategoryAdapter(List<String> categories, CategoryManager categoryManager) {
        this.categories = categories;
        this.categoryManager = categoryManager;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.manage_list_item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.categoryName.setText(categories.get(position));
        holder.delete.setOnClickListener(view -> categoryManager.deleteCategory(holder.categoryName.getText().toString()));
        holder.rename.setOnClickListener(view -> categoryManager.renameCategory(holder.categoryName.getText().toString()));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        Button delete;
        Button rename;

        ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.category_name);
            delete = itemView.findViewById(R.id.delete_category);
            rename = itemView.findViewById(R.id.rename_category);
        }
    }
}
```