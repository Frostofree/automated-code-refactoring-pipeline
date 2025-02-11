```java
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Map<String, List<Category>> flattenCategories() {
        Map<String, List<Category>> flattenedCategories = new HashMap<>();
        flattenCategories(flattenedCategories, categoryRepository.findAll());
        return flattenedCategories;
    }

    private void flattenCategories(Map<String, List<Category>> flattenedCategories, List<Category> categories) {
        for (Category category : categories) {
            if (category.getParent() == null) {
                StringBuilder prefix = new StringBuilder();
                while (category != null) {
                    if (prefix.length() > 0) {
                        prefix.append('-');
                    }
                    prefix.append(category.getName());
                    category = category.getParent();
                }
                flattenedCategories.put(prefix.toString(), categoryRepository.findAllByParent(category));
            }
        }
    }
}
```